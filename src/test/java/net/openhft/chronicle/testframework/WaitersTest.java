package net.openhft.chronicle.testframework;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WaitersTest {

    @Test
    void willThrowWhenConditionIsNotMet() {
        assertThrows(Waiters.ConditionNotMetException.class,
                () -> Waiters.waitForCondition("Not met", () -> false, 30), "Not met");
    }

    @Test
    void willReturnWhenConditionIsMet() {
        AtomicInteger counter = new AtomicInteger(0);
        Waiters.waitForCondition("Met", () -> counter.incrementAndGet() > 2, 100);
    }

    @Test
    void customMessageGeneration() {
        assertThrows(Waiters.ConditionNotMetException.class,
                () -> Waiters.builder(() -> "value", value -> false)
                        .messageGenerator(value -> "Wrong value: " + value)
                        .maxTimeToWaitMs(30)
                        .run(), "Wrong value: value");
    }

    @Test
    void settingPollInterval() {
        AtomicInteger counter = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();
        Waiters.builder(() -> counter.incrementAndGet() > 2)
                .checkIntervalMs(30)
                .run();
        final long elapsedTime = System.currentTimeMillis() - startTime;
        assertTrue(elapsedTime >= 60, "Elapsed time was " + elapsedTime);
    }
}