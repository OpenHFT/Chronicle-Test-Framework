package net.openhft.chronicle.testframework.apimetrics;

import net.openhft.chronicle.testframework.internal.apimetrics.StandardApiMetricsBuilder;

import java.util.function.Supplier;
import java.util.stream.Stream;

public interface ApiMetrics {

    /**
     * Returns a stream of Accumulators used for public (non-internal) packages.
     *
     * @return a stream of public Accumulators
     */
    Stream<Accumulator> accumulators();

    /**
     * Returns a stream of Accumulators used for internal packages.
     *
     * @return a stream of public Accumulators
     */
    Stream<Accumulator> internalAccumulators();


    /**
     * Creates and returns a new ApiMetricsBuilder.
     *
     * @return a new ApiMetricsBuilder
     */
    static ApiMetricsBuilder builder() {
        return new StandardApiMetricsBuilder();
    }



    interface ApiMetricsBuilder {

        /**
         * Adds the provided {@code paket} and all its underlying packages to the set of packages to analyze.
         *
         * @param paket to analyze (non-null)
         * @return this builder
         */
        default ApiMetricsBuilder addPackage(final Package paket) {
            return addPackage(paket.getName());
        }

        /**
         * Adds the provided {@code packageName} and all its underlying packages to the set of packages to analyze.
         *
         * @param packageName to analyze (non-null)
         * @return this builder
         */
        ApiMetricsBuilder addPackage(final String packageName);

        /**
         * Adds the provided {@code paket} and all its underlying packages to the set of excluded packages not to analyze.
         *
         * @param paket not to analyze (non-null)
         * @return this builder
         */
        default ApiMetricsBuilder addPackageExclusion(final Package paket) {
            return addPackageExclusion(paket.getName());
        }

        /**
         * Adds the provided {@code packageName} and all its underlying packages to the set of excluded packages not to analyze.
         *
         * @param packageName not to analyze (non-null)
         * @return this builder
         */
        ApiMetricsBuilder addPackageExclusion(final String packageName);

        /**
         * Add the provided {@code metric} as applicable when analysing the set of packages.
         *
         * @param metric to add (non-null)
         * @return this builder
         */
        ApiMetricsBuilder addMetric(final Metric<?> metric);

        /**
         * Add a set of standard metrics as applicable when analysing the set of packages.
         *
         * @return this builder
         */
        ApiMetricsBuilder addStandardMetrics();

        ApiMetricsBuilder addAccumulator(final Supplier<Accumulator> accumulator);

        ApiMetricsBuilder addStandardAccumulators();

        /**
         * Analyses and returns ApiMetrics thereby applying all the metrics and accumulators to the set of packages to analyse.
         *
         * @return the ApiMetrics
         */
        ApiMetrics build();
    }

}