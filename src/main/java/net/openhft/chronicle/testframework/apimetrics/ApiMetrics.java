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
     * Returns a stream of Accumulators used for public (non-internal) packages.
     * These Accumulators represent the metrics collected for visible or externally-facing parts of the API.
     *
     * @return a stream of public Accumulators
     */
    Stream<Accumulator> accumulators();

    /**
     * Returns a stream of Accumulators used for internal packages.
     * These Accumulators represent the metrics collected for hidden or internally-used parts of the API.
     *
     * @return a stream of internal Accumulators
     */
    Stream<Accumulator> internalAccumulators();

    /**
     * Creates and returns a new ApiMetricsBuilder.
     * This method facilitates the creation of ApiMetrics instances with custom configurations,
     * allowing flexibility in defining how the metrics are collected and aggregated.
     *
     * @return a new ApiMetricsBuilder instance
     */
    static ApiMetricsBuilder builder() {
        return new StandardApiMetricsBuilder();
    }


    interface ApiMetricsBuilder {

        /**
         * Adds the provided {@code paket} and all its underlying packages to the set of packages to analyze.
         * This method helps in specifying the packages that need to be included in the metrics analysis.
         *
         * @param paket Package to analyze (non-null)
         * @return this builder
         */
        default ApiMetricsBuilder addPackage(final Package paket) {
            return addPackage(paket.getName());
        }

        /**
         * Adds the provided {@code packageName} and all its underlying packages to the set of packages to analyze.
         * This method helps in specifying the packages that need to be included in the metrics analysis by name.
         *
         * @param packageName String representing the package to analyze (non-null)
         * @return this builder
         */
        ApiMetricsBuilder addPackage(final String packageName);

        /**
         * Adds the provided {@code paket} and all its underlying packages to the set of excluded packages not to analyze.
         * This method helps in specifying the packages that should be excluded from the metrics analysis.
         *
         * @param paket Package not to analyze (non-null)
         * @return this builder
         */
        default ApiMetricsBuilder addPackageExclusion(final Package paket) {
            return addPackageExclusion(paket.getName());
        }

        /**
         * Adds the provided {@code packageName} and all its underlying packages to the set of excluded packages not to analyze.
         * This method helps in specifying the packages that should be excluded from the metrics analysis by name.
         *
         * @param packageName String representing the package not to analyze (non-null)
         * @return this builder
         */
        ApiMetricsBuilder addPackageExclusion(final String packageName);

        /**
         * Add the provided {@code metric} as applicable when analysing the set of packages.
         * This allows customization of the metrics used in the analysis.
         *
         * @param metric Metric to add (non-null)
         * @return this builder
         */
        ApiMetricsBuilder addMetric(final Metric<?> metric);

        /**
         * Add a set of standard metrics as applicable when analysing the set of packages.
         * This method includes a predefined set of metrics suitable for general analysis.
         *
         * @return this builder
         */
        ApiMetricsBuilder addStandardMetrics();

        /**
         * Adds the supplied accumulator to the builder.
         * Accumulators help in aggregating metrics over a specified set of classes, methods, or fields.
         *
         * @param accumulator Supplier for the Accumulator to be added
         * @return this builder
         */
        ApiMetricsBuilder addAccumulator(final Supplier<Accumulator> accumulator);

        /**
         * Adds a set of standard accumulators to the builder.
         * Standard accumulators provide common aggregation functionalities for metrics analysis.
         *
         * @return this builder
         */
        ApiMetricsBuilder addStandardAccumulators();

        /**
         * Analyses and returns ApiMetrics thereby applying all the metrics and accumulators to the set of packages to analyse.
         * This method constructs and returns the final ApiMetrics object, ready for use.
         *
         * @return the ApiMetrics
         */
        ApiMetrics build();
    }
}