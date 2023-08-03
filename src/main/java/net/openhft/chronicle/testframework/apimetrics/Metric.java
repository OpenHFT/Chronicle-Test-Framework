package net.openhft.chronicle.testframework.apimetrics;

import net.openhft.chronicle.testframework.internal.apimetrics.StandardMetric;

import java.util.function.Predicate;

/**
 * The Metric interface provides a contract for defining metrics
 * that can be applied to different types like classes, methods, and fields.
 * This is used to measure various properties, such as visibility and extendability,
 * based on defined weights for each metric.
 *
 * @param <T> The type of element to which this metric can be applied.
 */
public interface Metric<T> {

    /**
     * Gets the class type that the metric is applicable to.
     *
     * @return The class type.
     */
    Class<T> nodeType();

    /**
     * Checks whether the metric is applicable to the given node.
     *
     * @param node The node to check.
     * @return True if the metric is applicable, false otherwise.
     */
    boolean isApplicable(T node);

    /**
     * Gets the weight of the metric. The weight represents the importance or
     * the value of the metric and can be used to calculate a cumulative score.
     *
     * @return The weight of the metric.
     */
    double weight();

    /**
     * Factory method to create a new instance of a Metric with the given parameters.
     *
     * @param <T>      The type of element to which this metric can be applied.
     * @param nodeType The class type of the element.
     * @param filter   The predicate to determine if the metric is applicable.
     * @param name     The name of the metric.
     * @param weight   The weight of the metric.
     * @return A new Metric instance.
     */
    static <T> Metric<T> of(final Class<T> nodeType,
                            final Predicate<? super T> filter,
                            final String name,
                            final double weight) {
        return new StandardMetric<>(nodeType, filter, name, weight);
    }
}
