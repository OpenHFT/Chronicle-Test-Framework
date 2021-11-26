package net.openhft.chronicle.testframework.apimetrics;

import net.openhft.chronicle.testframework.internal.apimetrics.StandardApiMetricsBuilder;

import java.util.function.Supplier;
import java.util.stream.Stream;

public interface ApiMetrics {

    Stream<Accumulator> accumulators();

    static ApiMetricsBuilder builder() {
        return new StandardApiMetricsBuilder();
    }

    interface ApiMetricsBuilder {

        default ApiMetricsBuilder addPackage(final Package paket) {
            return addPackage(paket.getName());
        }

        ApiMetricsBuilder addPackage(final String packageName);

        ApiMetricsBuilder addMetric(final Metric<?> metric);

        ApiMetricsBuilder addStandardMetrics();

        ApiMetricsBuilder addAccumulator(final Supplier<Accumulator> accumulator);

        ApiMetricsBuilder addStandardAccumulators();

        ApiMetrics build();
    }

}