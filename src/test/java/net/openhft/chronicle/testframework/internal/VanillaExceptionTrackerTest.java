package net.openhft.chronicle.testframework.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertThrows;

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
                ExceptionHolder::isIgnore);
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

    private static class ExceptionHolder {
        private final String description;
        private final Throwable exception;
        private final boolean ignore;

        private ExceptionHolder(String description, Throwable exception, boolean ignore) {
            this.description = description;
            this.exception = exception;
            this.ignore = ignore;
        }

        public String getDescription() {
            return description;
        }

        public Throwable getException() {
            return exception;
        }

        public boolean isIgnore() {
            return ignore;
        }
    }
}