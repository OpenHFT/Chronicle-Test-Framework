/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.testframework.internal;

import net.openhft.chronicle.testframework.FlakyTestRunner;
import net.openhft.chronicle.testframework.FlakyTestRunner.RunnableThrows;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class VanillaFlakyTestRunnerTest {

    @Test
    void runZero() {
        final MyAction action = new MyAction(0);
        assertDoesNotThrow(() ->
                FlakyTestRunner.builder(action)
                        .withMaxIterations(0)
                        .build()
                        .run()
        );
        assertEquals(0, action.countDown);
    }

    @Test
    void run() {
        final RunnableThrows<IllegalStateException> action = new MyAction(0);
        final RunnableThrows<IllegalStateException> runner = FlakyTestRunner.builder(action)
                .withMaxIterations(1)
                .build();

        assertDoesNotThrow(runner::run);
    }

    @Test
    void runTry2() {
        final RunnableThrows<IllegalStateException> action = new MyAction(1);
        final RunnableThrows<IllegalStateException> runner = FlakyTestRunner.builder(action)
                .withMaxIterations(2)
                .build();

        assertDoesNotThrow(runner::run);
    }

    @Test
    void runTry3() {
        final AtomicInteger attempts = new AtomicInteger();
        final IllegalStateException failure = new IllegalStateException();
        final RunnableThrows<IllegalStateException> action = () -> {
            attempts.incrementAndGet();
            throw failure;
        };
        final RunnableThrows<IllegalStateException> runner = FlakyTestRunner.builder(action)
                .withMaxIterations(2)
                .withIterationDelay(0)
                .withInterIterationGc(false)
                .build();

        assertSame(failure, assertThrows(IllegalStateException.class, runner::run));
        assertSame(failure, assertThrows(IllegalStateException.class, runner::run));
        assertEquals(4, attempts.get());
    }

    @Test
    void successLoggerFailureDoesNotRetryTheAction() {
        AtomicInteger attempts = new AtomicInteger();
        IllegalStateException failure = new IllegalStateException("logger unavailable");
        RunnableThrows<RuntimeException> runner = FlakyTestRunner.builderUnchecked(() -> {
            if (attempts.incrementAndGet() == 1)
                throw new AssertionError("first attempt fails");
        }).withMaxIterations(3).withIterationDelay(0).withInterIterationGc(false)
                .withInfoLogger(message -> { throw failure; }).build();

        assertSame(failure, assertThrows(IllegalStateException.class, runner::run));
        assertEquals(2, attempts.get());
        runner.run();
        assertEquals(3, attempts.get());
    }

    @Test
    void testNoThrow() {
        final RunnableThrows<?> runner = FlakyTestRunner.builder(this::foo)
                .build();

        assertDoesNotThrow(() -> runner.runOrThrow());
    }

    @Test
    void testUnchecked() {
        final RunnableThrows<RuntimeException> runner = FlakyTestRunner.builderUnchecked(this::foo)
                .build();

        assertDoesNotThrow(runner::run);
    }

    private void foo() {
        // Do nothing and throw nothing
    }

    @Test
    void demo() {
        RunnableThrows<IllegalStateException> action = new MyAction(2);

        final List<String> errors = new ArrayList<>();

        assertThrows(IllegalStateException.class, () ->
                FlakyTestRunner.builder(action)
                        .withFlakyOnThisArchitecture(isWindows())
                        .withMaxIterations(2)
                        .withInterIterationGc(false)
                        .withIterationDelay(200)
                        // Use a list rather than the default System.err
                        .withErrorLogger(errors::add)
                        .build()
                        .run()
        );
        assertFalse(errors.isEmpty());
    }

    private boolean isWindows() {
        return false;
    }

    private static final class MyAction implements RunnableThrows<IllegalStateException> {

        private int countDown;

        MyAction(int countDown) {
            this.countDown = countDown;
        }

        @Override
        public void run() throws IllegalStateException {
            if (countDown <= 0)
                return;
            throw new IllegalStateException("" + countDown--);
        }
    }
}
