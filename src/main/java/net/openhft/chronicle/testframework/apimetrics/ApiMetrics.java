/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.testframework.apimetrics;

import net.openhft.chronicle.testframework.internal.apimetrics.StandardApiMetricsBuilder;

import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Container for the aggregated results of API metric analysis. Metrics collected
 * from packages that are not marked as {@code internal} are kept separate from
 * those found in internal packages. Results are retrieved via
 * {@link #accumulators()} for public packages and
 * {@link #internalAccumulators()} for internal ones.
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
     * {@link #addPackageExclusion(String)} removes unwanted packages. If no
     * package is added the build will succeed but no classes will be examined.
     * At least one metric must be added otherwise {@link #build()} will throw an
     * {@link IllegalStateException}. Accumulators define how results are
     * aggregated and may be omitted.
     */
    interface ApiMetricsBuilder {

        /**
         * Adds the supplied {@code paket} and its sub-packages to the scan list.
         * Internally invokes {@code addPackage(paket.getName())}.
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
         * Adds Chronicle's default metrics. These cover counts of public and
         * protected classes, methods and fields, giving a quick view of the
         * exposed API surface.
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
         * Adds Chronicle's default accumulators. The set aggregates by metric,
         * package and class, providing an overview from several angles.
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
