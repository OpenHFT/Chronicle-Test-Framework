package net.openhft.chronicle.testframework.apimetrics;

import net.openhft.chronicle.testframework.internal.apimetrics.StandardApiMetricsBuilder;

import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * The ApiMetrics interface represents a contract for gathering and managing API metrics.
 * It provides methods to access accumulators for both public (non-internal) and internal packages,
 * allowing granular control and understanding of metrics across different parts of the application.
 */
public interface ApiMetrics {

    /**
     * Provides the accumulators applied to non-internal packages in the order they were added.
     *
     * @return stream of public accumulators
     */
    Stream<Accumulator> accumulators();

    /**
     * Provides the accumulators applied to internal packages in the order they were added.
     *
     * @return stream of internal accumulators
     */
    Stream<Accumulator> internalAccumulators();

    /**
     * Returns a fresh builder for {@link ApiMetrics}.
     *
     * @return new builder
     */
    static ApiMetricsBuilder builder() {
        return new StandardApiMetricsBuilder();
    }

    /**
     * Builder used to configure and create {@link ApiMetrics}.
     * <p>
     * Packages to scan are registered with {@link #addPackage(String)} while
     * {@link #addPackageExclusion(String)} removes unwanted packages. At least
     * one metric must be added otherwise {@link #build()} will throw an
     * {@link IllegalStateException}. Accumulators define how results are
     * aggregated and may be omitted.
     */
    interface ApiMetricsBuilder {

        /**
         * Adds the supplied {@code paket} and its sub-packages to the scan list.
         *
         * @param paket package to analyse (non-null)
         * @return this builder
         */
        default ApiMetricsBuilder addPackage(final Package paket) {
            return addPackage(paket.getName());
        }

        /**
         * Adds the named package and its sub-packages to the scan list.
         *
         * @param packageName package to analyse (non-null)
         * @return this builder
         */
        ApiMetricsBuilder addPackage(final String packageName);

        /**
         * Excludes the supplied {@code paket} and its sub-packages from scanning.
         *
         * @param paket package not to analyse (non-null)
         * @return this builder
         */
        default ApiMetricsBuilder addPackageExclusion(final Package paket) {
            return addPackageExclusion(paket.getName());
        }

        /**
         * Excludes the named package and its sub-packages from scanning.
         *
         * @param packageName package not to analyse (non-null)
         * @return this builder
         */
        ApiMetricsBuilder addPackageExclusion(final String packageName);

        /**
         * Registers a metric to apply when analysing the packages.
         *
         * @param metric metric to add (non-null)
         * @return this builder
         */
        ApiMetricsBuilder addMetric(final Metric<?> metric);

        /**
         * Adds Chronicle's default metrics.
         *
         * @return this builder
         */
        ApiMetricsBuilder addStandardMetrics();

        /**
         * Adds an accumulator used to aggregate metric values.
         *
         * @param accumulator supplier for the accumulator
         * @return this builder
         */
        ApiMetricsBuilder addAccumulator(final Supplier<Accumulator> accumulator);

        /**
         * Adds Chronicle's default accumulators.
         *
         * @return this builder
         */
        ApiMetricsBuilder addStandardAccumulators();

        /**
         * Scans the configured packages and returns the aggregated metrics.
         *
         * @return the ApiMetrics
         * @throws IllegalStateException if no metrics have been added
         */
        ApiMetrics build();
    }
}
