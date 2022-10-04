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
    void testHasException() {
        exceptionCounts.put(new ExceptionHolder("was present", null, false), 1);
        assertTrue(vet.hasException(eh -> eh.description.equals("was present")));
        assertFalse(vet.hasException(eh -> eh.description.equals("not present")));
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

        public String getDescription() {
            return description;
        }

        public Throwable getException() {
            return exception;
        }

        public boolean isFilter() {
            return filter;
        }
    }
}