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

    /**
     * Returns an unmodifiable list of names used in one or more aggregation depths.
     *
     * @return list of names
     */
    List<String> aggregationNames();

    /**
     * Merges the provided parameter into this accumulator mutating itself.
     *
     * @param metric applicable
     * @param classInfo ClassInfo applicable for the leaf
     * @param leaf ClassInfo, MethodInfo or FieldInfo provided
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
     *
     * @return map of first column keys to totals
     */
    Map<String, Double> result1();

    /**
     * Returns the aggregation grouped by the first and second columns.
     *
     * @return nested map of column keys to totals
     */
    Map<String, Map<String, Double>> result2();

    /**
     * Creates a level one accumulator using an always true predicate.
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
     * @return supplier for the standard per-method accumulator
     */
    static Supplier<Accumulator> perMethod() {
        return StandardAccumulators.PER_METHOD;
    }

    /**
     * Convenience factory for an accumulator per class and metric.
     *
     * @return supplier for the standard per-class and metric accumulator
     */
    static Supplier<Accumulator> perClassAndMetric() {
        return StandardAccumulators.PER_CLASS_AND_METRIC;
    }
}
