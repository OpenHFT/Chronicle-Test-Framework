/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.testframework.internal.apimetrics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StandardMetricsTest {

    @Test
    void stream() {
        // There should be no name duplicates
        final long total = StandardMetrics.stream().count();
        final long distinct = StandardMetrics.stream().map(Object::toString).distinct().count();
        assertEquals(total, distinct, "metric names are unique");
    }
}
