package net.openhft.chronicle.testframework.internal.apimetrics;

import net.openhft.chronicle.testframework.apimetrics.Accumulator;
import net.openhft.chronicle.testframework.apimetrics.ApiMetrics;
import org.junit.jupiter.api.Test;

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

        metrics.accumulators()
                .forEach(System.out::println);

    }

}