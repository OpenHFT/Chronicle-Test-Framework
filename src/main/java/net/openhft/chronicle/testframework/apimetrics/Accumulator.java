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

public interface Accumulator {

    List<String> aggregationNames();

    void accept(Metric<?> metric, ClassInfo classInfo, HasName leaf);

    Double result();

    Map<String, Double> result1();

    Map<String, Map<String, Double>> result2();

    static Accumulator of(String columnName,
                          Product.TriFunction<Metric<?>, ClassInfo, HasName, String> keyExtractor) {
        requireNonNull(columnName);
        requireNonNull(keyExtractor);
        return of(columnName, keyExtractor, (m, ci, l) -> true);
    }

    static Accumulator of(String columnName,
                          Product.TriFunction<Metric<?>, ClassInfo, HasName, String> keyExtractor,
                          Product.TriFunction<Metric<?>, ClassInfo, HasName, Boolean> predicate) {
        requireNonNull(columnName);
        requireNonNull(keyExtractor);
        requireNonNull(predicate);
        return new StandardAccumulator1(columnName, keyExtractor, predicate);
    }

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

    static Supplier<Accumulator> perMethod() {
        return StandardAccumulators.PER_METHOD;
    }

    static Supplier<Accumulator> perClassAndMetric() {
        return StandardAccumulators.PER_CLASS_AND_METRIC;
    }

}
