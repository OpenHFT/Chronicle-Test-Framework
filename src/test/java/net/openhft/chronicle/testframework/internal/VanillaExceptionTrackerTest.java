/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.testframework.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class VanillaExceptionTrackerTest {

    private AtomicInteger resetCounter = new AtomicInteger(0);
    private Map<ExceptionHolder, Integer> exceptionCounts;
    private VanillaExceptionTracker<ExceptionHolder> vet;

    @BeforeEach
    void setUp() {
        resetCounter = new AtomicInteger(0);
        exceptionCounts = new HashMap<>();
        vet = new VanillaExceptionTracker<>(
                ExceptionHolder::getDescription,
                ExceptionHolder::getException,
                () -> {
                    resetCounter.incrementAndGet();
                    exceptionCounts.clear();
                },
                exceptionCounts,
                ExceptionHolder::isFilter);
    }

    @Test
    void noExceptionsWillPassCheck() {
        vet.checkExceptions();
    }

    @Test
    void expectedExceptionsWillNotFailCheck_Description() {
        vet.expectException("foobar");
        exceptionCounts.put(new ExceptionHolder("foobar", new IllegalStateException(), false), 1);
        vet.checkExceptions();
    }

    @Test
    void expectedExceptionsWillNotFailCheck_ThrowableMessage() {
        vet.expectException("foobar");
        exceptionCounts.put(new ExceptionHolder("something else", new IllegalStateException("foobar"), false), 1);
        vet.checkExceptions();
    }

    @Test
    void ignoredExceptionsWillNotFailCheck_Description() {
        vet.ignoreException("foobar");
        exceptionCounts.put(new ExceptionHolder("foobar", new IllegalStateException(), false), 1);
        vet.checkExceptions();
    }

    @Test
    void ignoredExceptionsWillNotFailCheck_ThrowableMessage() {
        vet.ignoreException("foobar");
        exceptionCounts.put(new ExceptionHolder("something else", new IllegalStateException("foobar"), false), 1);
        vet.checkExceptions();
    }

    @Test
    void missingExceptedExceptionsWillFailCheck() {
        vet.expectException("foobar");
        assertThrows(AssertionError.class, () -> vet.checkExceptions());
    }

    @Test
    void unexpectedExceptionsWillFailCheck() {
        exceptionCounts.put(new ExceptionHolder("unexpected exception", new IllegalStateException(), false), 1);
        assertThrows(AssertionError.class, () -> vet.checkExceptions());
    }

    @Test
    void exceptionsIgnoredByPredicateWillNotFailCheck() {
        exceptionCounts.put(new ExceptionHolder("test test", new IllegalStateException(), true), 1);
        vet.checkExceptions();
    }

    @Test
    void ignoredAndExpectedExceptionsWillPassCheckWhenPresent() {
        vet.expectException("test test");
        vet.ignoreException("test test");
        exceptionCounts.put(new ExceptionHolder("test test", new IllegalStateException(), false), 1);
        vet.checkExceptions();
    }

    @Test
    void ignoredAndExpectedExceptionsWillFailCheckWhenNotPresent() {
        vet.expectException("test test");
        vet.ignoreException("test test");
        assertThrows(AssertionError.class, () -> vet.checkExceptions());
    }

    @Test
    void filteredAndExpectedExceptionsWillPassCheckWhenPresent() {
        vet.expectException("test test");
        exceptionCounts.put(new ExceptionHolder("test test", new IllegalStateException(), true), 1);
        vet.checkExceptions();
    }

    @Test
    void resetRunnableIsCalledInCheck() {
        vet.checkExceptions();
        assertEquals(1, resetCounter.get());
    }

    @Test
    void missingExpectationStillResetsHandlers() {
        vet.expectException("missing");
        assertThrows(AssertionError.class, vet::checkExceptions);
        assertEquals(1, resetCounter.get());
        assertThrows(IllegalStateException.class, vet::checkExceptions);
        assertEquals(1, resetCounter.get());
    }

    @Test
    void unexpectedExceptionStillResetsHandlers() {
        exceptionCounts.put(new ExceptionHolder("unexpected", null, false), 1);
        AssertionError failure = assertThrows(AssertionError.class, vet::checkExceptions);
        assertEquals("1 exceptions were detected: unexpected", failure.getMessage());
        assertEquals(1, resetCounter.get());
    }

    @Test
    void renderingFailureIsPreservedWhenResetAlsoFails() {
        IllegalStateException renderingFailure = new IllegalStateException("renderer failed");
        AssertionError resetFailure = new AssertionError("reset failed");
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("unexpected", 1);
        VanillaExceptionTracker<String> tracker = new VanillaExceptionTracker<>(s -> s, s -> null,
                () -> { throw resetFailure; }, counts, s -> false, s -> { throw renderingFailure; });
        assertSame(renderingFailure, assertThrows(IllegalStateException.class, tracker::checkExceptions));
        assertArrayEquals(new Throwable[]{resetFailure}, renderingFailure.getSuppressed());
    }

    @Test
    void resetFailureAfterSuccessfulCheckIsPropagated() {
        IllegalStateException resetFailure = new IllegalStateException("reset failed");
        VanillaExceptionTracker<String> tracker = new VanillaExceptionTracker<>(s -> s, s -> null,
                () -> { throw resetFailure; }, new HashMap<>(), s -> false);
        assertSame(resetFailure, assertThrows(IllegalStateException.class, tracker::checkExceptions));
    }

    @Test
    void concurrentRecordingDuringRenderingUsesOneSnapshot() throws InterruptedException {
        Map<String, Integer> counts = Collections.synchronizedMap(new LinkedHashMap<>());
        counts.put("first", 1);
        counts.put("second", 2);
        ExecutorService writer = Executors.newSingleThreadExecutor();
        try {
            VanillaExceptionTracker<String> tracker = new VanillaExceptionTracker<>(s -> s, s -> null,
                    resetCounter::incrementAndGet, counts, s -> false, s -> {
                        if ("first".equals(s))
                            recordOnWorker(writer, counts);
                        return s;
                    });
            AssertionError failure = assertThrows(AssertionError.class, tracker::checkExceptions);
            assertEquals("2 exceptions were detected: first, second", failure.getMessage());
            assertEquals(3, counts.size());
            assertEquals(1, resetCounter.get());
        } finally {
            writer.shutdownNow();
            assertTrue(writer.awaitTermination(2, TimeUnit.SECONDS));
        }
    }

    @Test
    void concurrentRecordingDuringPredicateDoesNotMutateTraversal() throws InterruptedException {
        Map<String, Integer> counts = Collections.synchronizedMap(new LinkedHashMap<>());
        counts.put("first", 1);
        counts.put("second", 2);
        ExecutorService writer = Executors.newSingleThreadExecutor();
        try {
            VanillaExceptionTracker<String> tracker = new VanillaExceptionTracker<>(s -> s, s -> null,
                    resetCounter::incrementAndGet, counts, s -> false);
            assertFalse(tracker.hasException(s -> {
                recordOnWorker(writer, counts);
                return false;
            }));
            tracker.expectException(s -> {
                recordOnWorker(writer, counts);
                return "first".equals(s);
            }, "first");
            tracker.ignoreException(s -> true, "remaining");
            tracker.checkExceptions();
            assertEquals(1, resetCounter.get());
        } finally {
            writer.shutdownNow();
            assertTrue(writer.awaitTermination(2, TimeUnit.SECONDS));
        }
    }

    private static void recordOnWorker(ExecutorService writer, Map<String, Integer> counts) {
        try {
            writer.submit(() -> counts.merge("late", 1, Integer::sum)).get(2, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new AssertionError("Recording must not be blocked by a tracker callback", e);
        }
    }

    @Test
    void filteringPreservesRepeatsRecordedAfterTheSnapshot() {
        Map<String, Integer> counts = Collections.synchronizedMap(new LinkedHashMap<>());
        counts.put("expected", 1);
        counts.put("ignored", 1);
        VanillaExceptionTracker<String> tracker = new VanillaExceptionTracker<>(s -> s, s -> null,
                resetCounter::incrementAndGet, counts, s -> false);
        tracker.expectException(s -> {
            if (!"expected".equals(s))
                return false;
            counts.merge(s, 1, Integer::sum);
            return true;
        }, "expected");
        tracker.ignoreException(s -> {
            counts.merge(s, 1, Integer::sum);
            return true;
        }, "ignored");
        tracker.checkExceptions();
        assertEquals(Integer.valueOf(2), counts.get("expected"));
        assertEquals(Integer.valueOf(2), counts.get("ignored"));
        assertEquals(1, resetCounter.get());
    }

    @Test
    void snapshotCopiesEntriesWhileHoldingRecordingMonitor() {
        Map<String, Integer> counts = new LinkedHashMap<String, Integer>() {
            @Override
            public Set<Map.Entry<String, Integer>> entrySet() {
                assertTrue(Thread.holdsLock(this), "Copying live entries must use the recorders' monitor");
                return super.entrySet();
            }
        };
        counts.put("expected", 1);
        VanillaExceptionTracker<String> tracker = new VanillaExceptionTracker<>(s -> s, s -> null,
                resetCounter::incrementAndGet, counts, s -> false);
        assertTrue(tracker.hasException("expected"));
        tracker.expectException("expected");
        tracker.checkExceptions();
        assertEquals(1, resetCounter.get());
    }

    @Test
    void cannotIgnoreExceptionsAfterFinalised() {
        vet.checkExceptions();
        assertThrows(IllegalStateException.class, () -> vet.ignoreException("test"));
    }

    @Test
    void cannotExpectExceptionsAfterFinalised() {
        vet.checkExceptions();
        assertThrows(IllegalStateException.class, () -> vet.expectException("test"));
    }

    @Test
    void cannotCheckExceptionsAfterFinalised() {
        vet.checkExceptions();
        assertThrows(IllegalStateException.class, () -> vet.checkExceptions());
    }

    @Test
    void testHasExceptionByPredicate() {
        exceptionCounts.put(new ExceptionHolder("was present", null, false), 1);
        assertTrue(vet.hasException(eh -> eh.description.equals("was present")));
        assertFalse(vet.hasException(eh -> eh.description.equals("not present")));
    }

    @Test
    void testHasExceptionByMessage() {
        exceptionCounts.put(new ExceptionHolder("was present", new RuntimeException("Even nested", new Exception("Even deeply nested")), false), 1);
        assertTrue(vet.hasException("was present"));
        assertTrue(vet.hasException("Even nested"));
        assertTrue(vet.hasException("Even deeply nested"));
        assertFalse(vet.hasException("not present"));
    }

    @Test
    void expectationWillMatchWhenMessageIsNestedInThrowableMessageCauses() {
        ExceptionHolder exceptionKey = new ExceptionHolder("nested with nulls",
                new RuntimeException("no match",
                        new RuntimeException(null,
                                new RuntimeException("this string matches"))), false);
        exceptionCounts.put(exceptionKey, 1);
        vet.expectException("matches");
        vet.checkExceptions();
    }

    @Test
    void checkDoesNotGetLostInCircularReference() {
        exceptionCounts.put(new ExceptionHolder("self-caused matching", new SelfCausedException("this string matches"), false), 1);
        vet.expectException("matches");
        vet.checkExceptions();
    }

    @SuppressWarnings("serial")
    private static final class SelfCausedException extends Exception {

        SelfCausedException(String message) {
            super(message);
        }

        @Override
        public synchronized Throwable getCause() {
            return this;
        }
    }

    private static class ExceptionHolder {
        private final String description;
        private final Throwable exception;
        private final boolean filter;

        private ExceptionHolder(String description, Throwable exception, boolean filter) {
            this.description = description;
            this.exception = exception;
            this.filter = filter;
        }

        String getDescription() {
            return description;
        }

        Throwable getException() {
            return exception;
        }

        boolean isFilter() {
            return filter;
        }
    }
}
