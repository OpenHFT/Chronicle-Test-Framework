package net.openhft.chronicle.testframework.internal.apimetrics;

import io.github.classgraph.ClassInfo;
import io.github.classgraph.HasName;
import net.openhft.chronicle.testframework.Product;
import net.openhft.chronicle.testframework.apimetrics.Accumulator;
import net.openhft.chronicle.testframework.apimetrics.Metric;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Map.Entry.comparingByKey;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

public final class StandardAccumulator2 implements Accumulator {

    private final String columnName;
    private final Product.TriFunction<Metric<?>, ClassInfo, HasName, String> keyExtractor;
    private final String columnName2;
    private final Product.TriFunction<Metric<?>, ClassInfo, HasName, String> keyExtractor2;
    private final Product.TriFunction<Metric<?>, ClassInfo, HasName, Boolean> predicate;
    private final Map<String, Map<String, Double>> map = new HashMap<>();

    public StandardAccumulator2(final String columnName,
                                final Product.TriFunction<Metric<?>, ClassInfo, HasName, String> keyExtractor,
                                final String columnName2,
                                final Product.TriFunction<Metric<?>, ClassInfo, HasName, String> keyExtractor2,
                                final Product.TriFunction<Metric<?>, ClassInfo, HasName, Boolean> predicate) {
        this.columnName = requireNonNull(columnName);
        this.keyExtractor = requireNonNull(keyExtractor);
        this.columnName2 = requireNonNull(columnName2);
        this.keyExtractor2 = requireNonNull(keyExtractor2);
        this.predicate = requireNonNull(predicate);
    }

    @Override
    public List<String> aggregationNames() {
        return Arrays.asList(columnName, columnName2);
    }

    @Override
    public void accept(Metric<?> metric, ClassInfo classInfo, HasName leaf) {
        if (Boolean.TRUE.equals(predicate.apply(metric, classInfo, leaf))) {
            final String key = keyExtractor.apply(metric, classInfo, leaf);
            final String key2 = keyExtractor2.apply(metric, classInfo, leaf);
            map.computeIfAbsent(key, k -> new HashMap<>())
                    .merge(key2, metric.weight(), Double::sum);
        }
    }

    @Override
    public Double result() {
        return map.values().stream()
                .flatMap(m -> m.values().stream())
                .mapToDouble(d -> d)
                .sum();
    }

    @Override
    public Map<String, Double> result1() {
        return map.entrySet().stream()
                .collect(toMap(Map.Entry::getKey, e -> e.getValue().values().stream().mapToDouble(d -> d).sum()));

    }

    @Override
    public Map<String, Map<String, Double>> result2() {
        return Collections.unmodifiableMap(map);
    }

    @Override
    public String toString() {
        final int maxCol = map.keySet().stream()
                .mapToInt(String::length)
                .max()
                .orElse(10);

        final int maxCol2 = map.values().stream()
                .flatMap(m -> m.keySet().stream())
                .mapToInt(String::length)
                .max()
                .orElse(10);

        final String formatting = "%-" + maxCol + "s %-" + maxCol2 + "s %12.0f%n";

        return String.format("*Accumulation per %s and %s *%n", columnName, columnName2) +
                map.entrySet().stream()
                        .sorted(comparingByKey())
                        .flatMap(e -> e.getValue().entrySet().stream()
                                .sorted(comparingByKey())
                                .map(e2 -> String.format(formatting, e.getKey(), e2.getKey(), e2.getValue())))
                        .collect(Collectors.joining())
                + String.format(formatting, "_Total_", "", result());

    }
}
