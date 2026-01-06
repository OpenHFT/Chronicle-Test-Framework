/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.testframework.internal.apimetrics;

import net.openhft.chronicle.testframework.apimetrics.Accumulator;
import net.openhft.chronicle.testframework.apimetrics.ApiMetrics;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;

final class StandardApiMetricsBuilderTest {

    @Test
    void enumTest() {
        ApiMetrics metrics = new StandardApiMetricsBuilder()
                .addStandardMetrics()
                .addStandardAccumulators()
                .addAccumulator(Accumulator.perMethod())
                .addAccumulator(Accumulator.perClassAndMetric())
                .addPackage(Foo.class.getPackage())
                .build();

        final List<Accumulator> accumulators = metrics.accumulators().collect(Collectors.toList());
        assertFalse(accumulators.isEmpty(), "public accumulators collected");
        accumulators.forEach(System.out::println);

    }
}
