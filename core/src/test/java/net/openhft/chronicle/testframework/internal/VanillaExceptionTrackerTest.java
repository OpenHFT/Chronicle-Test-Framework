/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.testframework.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
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
        assertThrows(AssertionError.class, vet::checkExceptions, "missing expected exceptions fails check");
    }

    @Test
    void unexpectedExceptionsWillFailCheck() {
        exceptionCounts.put(new ExceptionHolder("unexpected exception", new IllegalStateException(), false), 1);
        assertThrows(AssertionError.class, vet::checkExceptions, "unexpected exceptions fail check");
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
        assertThrows(AssertionError.class, vet::checkExceptions, "expected exception missing still fails check");
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
        assertEquals(1, resetCounter.get(), "reset runnable called");
    }

    @Test
    void cannotIgnoreExceptionsAfterFinalised() {
        vet.checkExceptions();
        assertThrows(IllegalStateException.class, () -> vet.ignoreException("test"), "ignore rejected after finalise");
    }

    @Test
    void cannotExpectExceptionsAfterFinalised() {
        vet.checkExceptions();
        assertThrows(IllegalStateException.class, () -> vet.expectException("test"), "expect rejected after finalise");
    }

    @Test
    void cannotCheckExceptionsAfterFinalised() {
        vet.checkExceptions();
        assertThrows(IllegalStateException.class, vet::checkExceptions, "check rejected after finalise");
    }

    @Test
    void testHasExceptionByPredicate() {
        exceptionCounts.put(new ExceptionHolder("was present", null, false), 1);
        assertTrue(vet.hasException(eh -> eh.description.equals("was present")), "predicate finds existing exception");
        assertFalse(vet.hasException(eh -> eh.description.equals("not present")), "predicate does not match absent exception");
    }

    @Test
    void testHasExceptionByMessage() {
        exceptionCounts.put(new ExceptionHolder("was present", new RuntimeException("Even nested", new Exception("Even deeply nested")), false), 1);
        assertTrue(vet.hasException("was present"), "matches description");
        assertTrue(vet.hasException("Even nested"), "matches nested message");
        assertTrue(vet.hasException("Even deeply nested"), "matches deeply nested message");
        assertFalse(vet.hasException("not present"), "does not match absent message");
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
