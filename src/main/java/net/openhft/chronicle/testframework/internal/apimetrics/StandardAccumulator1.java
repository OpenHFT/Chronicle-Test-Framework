/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.testframework.internal.apimetrics;

import io.github.classgraph.ClassInfo;
import io.github.classgraph.HasName;
import net.openhft.chronicle.testframework.Product;
import net.openhft.chronicle.testframework.apimetrics.Accumulator;
import net.openhft.chronicle.testframework.apimetrics.Metric;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Aggregates metric weightings for a single column.
 *
 * <p>Each metric is examined by the supplied predicate. When the predicate
 * returns {@code true} the key extractor determines the column value and the
 * metric's weight is added to the running total for that key.</p>
 *
 * <p>The overall result is the sum of all recorded weights, while
 * {@link #result1()} exposes the per-key values.</p>
 */
public final class StandardAccumulator1 implements Accumulator {

    private final String columnName;
    private final Product.TriFunction<Metric<?>, ClassInfo, HasName, String> keyExtractor;
    private final Product.TriFunction<Metric<?>, ClassInfo, HasName, Boolean> predicate;
    private final Map<String, Double> map = new HashMap<>();

    public StandardAccumulator1(final String columnName,
                                final Product.TriFunction<Metric<?>, ClassInfo, HasName, String> keyExtractor,
                                final Product.TriFunction<Metric<?>, ClassInfo, HasName, Boolean> predicate) {
        this.columnName = requireNonNull(columnName);
        this.keyExtractor = requireNonNull(keyExtractor);
        this.predicate = requireNonNull(predicate);
    }

    /**
     * Returns the label used for this aggregation.
     */
    @Override
    public List<String> aggregationNames() {
        return Collections.singletonList(columnName);
    }

    /**
     * Adds the metric to the accumulation if it satisfies the predicate.
     *
     * @param metric     the metric under consideration
     * @param classInfo  class information for context
     * @param leaf       the method or field being measured
     */
    @Override
    public void accept(Metric<?> metric, ClassInfo classInfo, HasName leaf) {
        if (Boolean.TRUE.equals(predicate.apply(metric, classInfo, leaf))) {
            final String key = keyExtractor.apply(metric, classInfo, leaf);
            map.merge(key, metric.weight(), Double::sum);
        }
    }

    /**
     * Returns the sum of all accumulated weights.
     */
    @Override
    public Double result() {
        return map.values().stream()
                .mapToDouble(d -> d)
                .sum();
    }

    /**
     * Provides an unmodifiable view of the per-key totals.
     */
    @Override
    public Map<String, Double> result1() {
        return Collections.unmodifiableMap(map);
    }

    /**
     * This accumulator does not support a second level of grouping.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public Map<String, Map<String, Double>> result2() {
        throw new UnsupportedOperationException("This aggregation is of level 1");
    }

    /**
     * Produces a textual summary of the accumulation.
     */
    @Override
    public String toString() {
        final int maxCol = map.keySet().stream()
                .mapToInt(String::length)
                .max()
                .orElse(10);

        final String formatting = "%-" + maxCol + "s %12.0f%n";

        return String.format("*Accumulation per %s*%n", columnName) +
                map.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .map(e -> String.format(formatting, e.getKey(), e.getValue()))
                        .collect(Collectors.joining())
                + String.format(formatting, "_Total_", result());

    }
}
