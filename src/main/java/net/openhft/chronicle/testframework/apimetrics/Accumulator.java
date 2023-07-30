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

/**
 * The Accumulator interface represents a contract for accumulating metrics
 * for different types like classes, methods, and fields.
 * It provides methods for aggregation, acceptance, and retrieval of results.
 */
public interface Accumulator {

    /**
     * Returns an unmodifiable list of names used in one or more aggregation depths.
     *
     * @return list of names representing aggregation levels or categories.
     */
    List<String> aggregationNames();

    /**
     * Merges the provided metric into this accumulator, mutating itself.
     * This method allows the accumulator to accept and process a new metric,
     * considering the associated class and leaf information.
     *
     * @param metric The applicable metric to accumulate.
     * @param classInfo The class information applicable for the leaf.
     * @param leaf The leaf information (e.g., ClassInfo, MethodInfo, FieldInfo) provided.
     */
    void accept(Metric<?> metric, ClassInfo classInfo, HasName leaf);

    /**
     * Returns the final result of the accumulation as a single value.
     * This is a condensed representation of the aggregated metrics.
     *
     * @return The overall result of the accumulation.
     */
    Double result();

    /**
     * Returns the first level of accumulated results as a map.
     * This can be useful for understanding the contribution of individual metrics.
     *
     * @return A map containing the first level of results.
     */
    Map<String, Double> result1();

    /**
     * Returns the second level of accumulated results as a nested map.
     * This provides a detailed view of the metrics, organized by categories or levels.
     *
     * @return A map containing the second level of results, possibly categorized by name and other attributes.
     */
    Map<String, Map<String, Double>> result2();


  /**
 * Creates an Accumulator using a specified column name and key extractor function.
 *
 * @param columnName   The name of the column to use in the accumulation.
 * @param keyExtractor A tri-function to extract keys based on a metric, class information, and leaf information.
 * @return A new instance of an Accumulator.
 */
  static Accumulator of(String columnName,
                          Product.TriFunction<Metric<?>, ClassInfo, HasName, String> keyExtractor) {
        requireNonNull(columnName);
        requireNonNull(keyExtractor);
        return of(columnName, keyExtractor, (m, ci, l) -> true);
    }

/**
 * Creates an Accumulator using specified column name, key extractor, and predicate functions.
 *
 * @param columnName   The name of the column to use in the accumulation.
 * @param keyExtractor A tri-function to extract keys based on a metric, class information, and leaf information.
 * @param predicate    A tri-function to provide additional filtering based on a metric, class information, and leaf information.
 * @return A new instance of an Accumulator.
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
 * Creates an Accumulator with two columns using specified column names and key extractors.
 *
 * @param columnName    The name of the first column.
 * @param keyExtractor  A tri-function to extract keys for the first column.
 * @param columnName2   The name of the second column.
 * @param keyExtractor2 A tri-function to extract keys for the second column.
 * @return A new instance of an Accumulator.
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
 * Creates an Accumulator with two columns using specified column names, key extractors, and a predicate function.
 *
 * @param columnName    The name of the first column.
 * @param keyExtractor  A tri-function to extract keys for the first column.
 * @param columnName2   The name of the second column.
 * @param keyExtractor2 A tri-function to extract keys for the second column.
 * @param predicate     A tri-function to provide additional filtering.
 * @return A new instance of an Accumulator.
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
 * Creates a Supplier for Accumulators configured for per-method accumulation.
 *
 * @return A Supplier for per-method Accumulators.
 */
    static Supplier<Accumulator> perMethod() {
        return StandardAccumulators.PER_METHOD;
    }

/**
 * Creates a Supplier for Accumulators configured for per-class and per-metric accumulation.
 *
 * @return A Supplier for per-class and per-metric Accumulators.
 */
    static Supplier<Accumulator> perClassAndMetric() {
        return StandardAccumulators.PER_CLASS_AND_METRIC;
    }


}
