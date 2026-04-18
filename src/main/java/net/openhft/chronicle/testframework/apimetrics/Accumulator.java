/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.testframework.apimetrics;

import io.github.classgraph.ClassInfo;
import io.github.classgraph.HasName;
import net.openhft.chronicle.testframework.Product;
import net.openhft.chronicle.testframework.internal.apimetrics.StandardAccumulator1;
import net.openhft.chronicle.testframework.internal.apimetrics.StandardAccumulator2;
import net.openhft.chronicle.testframework.internal.apimetrics.StandardAccumulators;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Collects metric weights during API analysis.
 *
 * <p>Instances are supplied with {@link Metric metrics} by
 * {@link ApiMetrics} as classes and members are scanned. Each call to
 * {@link #accept(Metric, ClassInfo, HasName)} adds the metric's weight to a
 * running total under keys derived from the analysed element.</p>
 *
 * <p>Aggregated results may then be obtained as overall totals or grouped
 * views via {@link #result()}, {@link #result1()} and {@link #result2()}.</p>
 */
public interface Accumulator {

    /**
     * Returns an unmodifiable list of names used in one or more aggregation depths.
     *
     * @return list of names
     */
    List<String> aggregationNames();

    /**
     * Adds the supplied metric to this accumulator.
     * <p>
     * {@code classInfo} denotes the declaring class of {@code leaf}. When
     * {@code leaf} is itself a {@link ClassInfo} the two parameters refer to the
     * same object.
     *
     * @param metric     metric under consideration
     * @param classInfo  enclosing class information
     * @param leaf       class, method or field being measured
     */
    void accept(Metric<?> metric, ClassInfo classInfo, HasName leaf);

    /**
     * Returns the total value accumulated across all keys.
     *
     * @return aggregated total
     */
    Double result();

    /**
     * Returns the aggregation grouped by the first column only.
     * <p>
     * Keys are derived from the first key extractor and each value is the sum
     * of metric weights for that key. The returned map is unmodifiable and may
     * contain a {@code null} key when a key extractor yields {@code null}.
     *
     * @return map from first column keys to aggregated weights
     */
    Map<String, Double> result1();

    /**
     * Returns the aggregation grouped by the first and second columns.
     * <p>
     * The outer map is keyed by values from the first extractor and the inner
     * map is keyed by the second. Each entry holds the sum of metric weights for
     * that pair. Implementations that do not support two-level grouping must
     * throw {@link UnsupportedOperationException}.
     *
     * @return nested map from first and second keys to aggregated weights
     * @throws UnsupportedOperationException if two-level grouping is not supported
     */
    Map<String, Map<String, Double>> result2();

    /**
     * Creates a level one accumulator using an always true predicate.
     *
     * <p>Null keys returned by the extractor are allowed and will be
     * accumulated under a {@code null} entry.</p>
     *
     * @param columnName   name of the aggregation column
     * @param keyExtractor function used to extract the key
     * @return configured Accumulator
     */
    static Accumulator of(String columnName,
                          Product.TriFunction<Metric<?>, ClassInfo, HasName, String> keyExtractor) {
        requireNonNull(columnName);
        requireNonNull(keyExtractor);
        return of(columnName, keyExtractor, (m, ci, l) -> true);
    }

    /**
     * Creates a level one accumulator using the supplied predicate.
     *
     * <p>Null keys returned by the extractor are allowed and will be
     * accumulated under a {@code null} entry.</p>
     *
     * @param columnName   name of the aggregation column
     * @param keyExtractor function used to extract the key
     * @param predicate    filter applied before accumulation
     * @return configured Accumulator
     */
    static Accumulator of(String columnName,
                          Product.TriFunction<Metric<?>, ClassInfo, HasName, String> keyExtractor,
                          Product.TriFunction<Metric<?>, ClassInfo, HasName, Boolean> predicate) {
        requireNonNull(columnName);
        requireNonNull(keyExtractor);
        requireNonNull(predicate);
        return new StandardAccumulator1(columnName, keyExtractor, predicate);
    }

    /**
     * Creates a level two accumulator using an always true predicate.
     *
     * <p>Null keys from either extractor are allowed and will be stored using
     * a {@code null} map key.</p>
     *
     * @param columnName    name of the first aggregation column
     * @param keyExtractor  function used to extract the first key
     * @param columnName2   name of the second aggregation column
     * @param keyExtractor2 function used to extract the second key
     * @return configured Accumulator
     */
    static Accumulator of(final String columnName,
                          final Product.TriFunction<Metric<?>, ClassInfo, HasName, String> keyExtractor,
                          final String columnName2,
                          final Product.TriFunction<Metric<?>, ClassInfo, HasName, String> keyExtractor2) {
        requireNonNull(columnName);
        requireNonNull(keyExtractor);
        requireNonNull(columnName2);
        requireNonNull(keyExtractor2);
        return of(columnName, keyExtractor, columnName2, keyExtractor2, (m, ci, l) -> true);
    }

    /**
     * Creates a level two accumulator using the supplied predicate.
     *
     * <p>Null keys from either extractor are allowed and will be stored using
     * a {@code null} map key.</p>
     *
     * @param columnName    name of the first aggregation column
     * @param keyExtractor  function used to extract the first key
     * @param columnName2   name of the second aggregation column
     * @param keyExtractor2 function used to extract the second key
     * @param predicate     filter applied before accumulation
     * @return configured Accumulator
     */
    static Accumulator of(final String columnName,
                          final Product.TriFunction<Metric<?>, ClassInfo, HasName, String> keyExtractor,
                          final String columnName2,
                          final Product.TriFunction<Metric<?>, ClassInfo, HasName, String> keyExtractor2,
                          final Product.TriFunction<Metric<?>, ClassInfo, HasName, Boolean> predicate) {
        requireNonNull(columnName);
        requireNonNull(keyExtractor);
        requireNonNull(columnName2);
        requireNonNull(keyExtractor2);
        requireNonNull(predicate);
        return new StandardAccumulator2(columnName, keyExtractor, columnName2, keyExtractor2, predicate);
    }

    /**
     * Convenience factory for an accumulator per method.
     *
     * <p>Each call returns a supplier that creates a fresh accumulator grouping
     * metrics by the fully qualified method signature, including parameter
     * types.</p>
     *
     * @return supplier for the standard per-method accumulator
     */
    static Supplier<Accumulator> perMethod() {
        return StandardAccumulators.PER_METHOD;
    }

    /**
     * Convenience factory for an accumulator per class and metric.
     *
     * <p>The resulting accumulator groups first by class name and then by the
     * metric's identifying name, providing a class-centric view of the
     * analysis.</p>
     *
     * @return supplier for the standard per-class and metric accumulator
     */
    static Supplier<Accumulator> perClassAndMetric() {
        return StandardAccumulators.PER_CLASS_AND_METRIC;
    }
}
