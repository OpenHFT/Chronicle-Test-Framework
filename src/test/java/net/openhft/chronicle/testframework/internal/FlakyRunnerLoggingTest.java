/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.testframework.internal;

import net.openhft.chronicle.testframework.FlakyTestRunner;
import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class FlakyRunnerLoggingTest {
    @Test
    void loggingFailureDoesNotRepeatAnActionThatAlreadySucceeded() throws Throwable {
        AtomicInteger attempts = new AtomicInteger();
        IllegalStateException loggingFailure = new IllegalStateException("logger unavailable");
        FlakyTestRunner.RunnableThrows<Throwable> runner = new VanillaFlakyTestRunnerBuilder<Throwable>(() -> {
            if (attempts.incrementAndGet() == 1)
                throw new AssertionError("first attempt fails");
        }).withMaxIterations(3).withIterationDelay(0).withInterIterationGc(false)
                .withErrorLogger(message -> { })
                .withInfoLogger(message -> { throw loggingFailure; }).build();
        assertSame(loggingFailure, assertThrows(IllegalStateException.class, runner::run));
        assertEquals(2, attempts.get(), "The successful action must not be retried because logging failed");
        runner.run();
        assertEquals(3, attempts.get(), "Runner must reset its re-entrancy guard after the logger fails");
    }

    @Test
    void finalActionFailureIsPropagatedAndRunnerCanBeReused() throws Throwable {
        AtomicInteger attempts = new AtomicInteger();
        AssertionError failure = new AssertionError("action fails");
        FlakyTestRunner.RunnableThrows<Throwable> runner = new VanillaFlakyTestRunnerBuilder<Throwable>(() -> {
            attempts.incrementAndGet();
            throw failure;
        }).withMaxIterations(2).withIterationDelay(0).withInterIterationGc(false)
                .withErrorLogger(message -> { }).build();
        assertSame(failure, assertThrows(AssertionError.class, runner::run));
        assertSame(failure, assertThrows(AssertionError.class, runner::run));
        assertEquals(4, attempts.get());
    }
}
