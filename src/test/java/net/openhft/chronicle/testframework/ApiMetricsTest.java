package net.openhft.chronicle.testframework;

import net.openhft.chronicle.testframework.apimetrics.Accumulator;
import net.openhft.chronicle.testframework.apimetrics.ApiMetrics;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ApiMetricsTest {

    @Test
    void test() {
        final ApiMetrics apiMetrics = ApiMetrics.builder()
                .addStandardMetrics()
                .addStandardAccumulators()
                .addAccumulator(Accumulator.perMethod())
                .addAccumulator(Accumulator.perClassAndMetric())
                .addPackage(ApiMetricsTest.class.getPackage())
                .build();

        System.out.println(apiMetrics);

        assertNotEquals(0, apiMetrics.accumulators().count());

        System.out.println("\"*******\" = " + "*******");

        apiMetrics.internalAccumulators()
                .forEach(System.out::println);


    }

}
