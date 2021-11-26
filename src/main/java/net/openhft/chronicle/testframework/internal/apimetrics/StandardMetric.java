package net.openhft.chronicle.testframework.internal.apimetrics;

import net.openhft.chronicle.testframework.apimetrics.Metric;

import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

public class StandardMetric<T> implements Metric<T> {

    private final Class<T> nodeType;
    private final Predicate<? super T> filter;
    private final String name;
    private final double weight;
    //private final double factor;

    public StandardMetric(final Class<T> nodeType,
                          final Predicate<? super T> filter,
                          final String name,
                          final double weight) {
        this.nodeType = requireNonNull(nodeType);
        this.filter = requireNonNull(filter);
        this.name = requireNonNull(name);
        this.weight = weight;
    }

    @Override
    public Class<T> nodeType() {
        return nodeType;
    }

    @Override
    public boolean isApplicable(T node) {
        return filter.test(node);
    }

    @Override
    public double weight() {
        return weight;
    }

    @Override
    public String toString() {
        return name;
    }
}
