package net.openhft.chronicle.testframework.apimetrics;

import net.openhft.chronicle.testframework.internal.apimetrics.StandardMetric;

import java.util.function.Predicate;

/**
 * A Metric defines a rule used when scoring an API element. Typical metrics
 * check for public or protected classes, methods that can be overridden and
 * public fields. Each metric carries a weight showing its relative importance.
 * For instance a public class may have weight 10 while a protected method may
 * weigh 1. Accumulators sum the weights of all applicable metrics to produce an
 * overall score.
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
