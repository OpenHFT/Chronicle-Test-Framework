/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.testframework.apimetrics;

import net.openhft.chronicle.testframework.internal.apimetrics.StandardMetric;

import java.util.function.Predicate;

/**
 * A Metric defines a rule used when scoring an API element. Typical metrics
 * check for public or protected classes, methods that can be overridden and
 * public fields. Each metric carries a weight indicating how much it
 * contributes to the final score. Higher weights have greater influence. For
 * instance a public class may have weight 10 while a protected method may weigh
 * 1. Accumulators sum the weights of all applicable metrics to produce an
 * overall score.
 *
 * <p>Example usage:</p>
 *
 * <pre>{@code
 * Metric<Class<?>> hasPublicConstructor = Metric.of(
 *         Class.class,
 *         c -> c.getConstructors().length > 0,
 *         "hasPublicConstructor",
 *         5);
 * }</pre>
 *
 * <p>This metric adds five points whenever a class exposes a public
 * constructor.</p>
 *
 * @param <T> the type of element to which this metric can be applied.
 */
public interface Metric<T> {

    /**
     * Gets the class type that the metric is applicable to.
     *
     * @return The class type.
     */
    Class<T> nodeType();

    /**
     * Checks whether the metric applies to the supplied node.
     *
     * @param node the element being evaluated
     * @return {@code true} if the metric should be counted; {@code false}
     *         otherwise
     */
    boolean isApplicable(T node);

    /**
     * Gets the weight of the metric. The weight is the amount added to the
     * cumulative score whenever {@link #isApplicable(Object)} returns
     * {@code true}.
     *
     * @return the weight of the metric
     */
    double weight();

    /**
     * Factory method to create a new instance of a Metric with the given parameters.
     *
     * @param <T>      The type of element to which this metric can be applied.
     * @param nodeType class the metric operates on
     * @param filter   predicate used by {@link #isApplicable(Object)}
     * @param name     human readable name for reporting
     * @param weight   value added to the score when applicable
     * @return A new Metric instance.
     */
    static <T> Metric<T> of(final Class<T> nodeType,
                            final Predicate<? super T> filter,
                            final String name,
                            final double weight) {
        return new StandardMetric<>(nodeType, filter, name, weight);
    }
}
