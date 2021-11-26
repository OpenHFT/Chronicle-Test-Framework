package net.openhft.chronicle.testframework.apimetrics;

import net.openhft.chronicle.testframework.internal.apimetrics.StandardMetric;

import java.util.function.Predicate;

public interface Metric<T> {

    Class<T> nodeType();

    boolean isApplicable(T node);

    double weight();

    // double factor();

    static <T> Metric<T> of(final Class<T> nodeType,
                            final Predicate<? super T> filter,
                            final String name,
                            final double weight) {
        return new StandardMetric<>(nodeType, filter, name, weight);
    }

}
