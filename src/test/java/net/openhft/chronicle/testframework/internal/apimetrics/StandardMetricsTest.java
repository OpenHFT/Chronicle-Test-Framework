package net.openhft.chronicle.testframework.internal.apimetrics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StandardMetricsTest {

    @Test
    void stream() {
        // There should be no name duplicates
        assertEquals(StandardMetrics.stream().map(Object::toString).distinct().count(), StandardMetrics.stream().count());
    }
}