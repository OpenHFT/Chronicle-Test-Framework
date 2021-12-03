package net.openhft.chronicle.testframework.internal.apimetrics;

import io.github.classgraph.ClassInfo;
import io.github.classgraph.HasName;
import net.openhft.chronicle.testframework.Product;
import net.openhft.chronicle.testframework.apimetrics.Accumulator;
import net.openhft.chronicle.testframework.apimetrics.Metric;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

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

    @Override
    public List<String> aggregationNames() {
        return Collections.singletonList(columnName);
    }

    @Override
    public void accept(Metric<?> metric, ClassInfo classInfo, HasName leaf) {
        if (Boolean.TRUE.equals(predicate.apply(metric, classInfo, leaf))) {
            final String key = keyExtractor.apply(metric, classInfo, leaf);
            map.merge(key, metric.weight(), Double::sum);
        }
    }

    @Override
    public Double result() {
        return map.values().stream()
                .mapToDouble(d -> d)
                .sum();
    }

    @Override
    public Map<String, Double> result1() {
        return Collections.unmodifiableMap(map);
    }

    @Override
    public Map<String, Map<String, Double>> result2() {
        throw new UnsupportedOperationException("This aggregation is of level 1");
    }

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
                +String.format(formatting, "_Total_", result());

    }
}
