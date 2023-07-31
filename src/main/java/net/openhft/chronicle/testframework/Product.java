package net.openhft.chronicle.testframework;

import net.openhft.chronicle.testframework.internal.ProductUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public final class Product {

    // Suppresses default constructor, ensuring non-instantiability.
    private Product() {
    }

    /**
     * Creates and returns a Stream representing the Cartesian product of the given {@code ts} and {@code us} collections.
     * <p>
     * This method returns a Stream of {@link Product2} objects, which pairs each element in {@code ts} with each
     * element in {@code us}. The order of the product is by {@code T} (most significant factor) and then {@code U}.
     * <p>
     * The provided collections must not be {@code null}, or a {@code NullPointerException} will be thrown.
     * <p>
     * Example:
     * If {@code ts = ["A", "B"]} and {@code us = [1, 2]}, the method will return a Stream with elements:
     * {@code [("A", 1), ("A", 2), ("B", 1), ("B", 2)]}.
     *
     * @param <T> The element type for the first collection
     * @param <U> The element type for the second collection
     * @param ts  The first collection of elements (non-null)
     * @param us  The second collection of elements (non-null)
     * @return the Cartesian product of the given elements, represented as a Stream of {@link Product2} objects
     * @throws NullPointerException if any of the provided collections are {@code null}
     */
    public static <T, U> Stream<Product2<T, U>> of(@NotNull final Collection<T> ts,
                                                   @NotNull final Collection<U> us) {
        // Ensure that the provided collections are not null
        requireNonNull(ts, "The first collection (ts) must not be null");
        requireNonNull(us, "The second collection (us) must not be null");

        // Utilize a helper method from ProductUtil to generate the Cartesian product and return as a stream
        return of(ts, us, ProductUtil.Product2Impl::new);
    }

    /**
     * Creates and returns the cartesian product of the given elements by applying the provided
     * {@code constructor} to each tuple (pair of elements).
     * <p>
     * This method calculates the Cartesian product of two collections, {@code ts} and {@code us},
     * by pairing each element in {@code ts} with each element in {@code us} and applying the provided
     * {@code constructor} to each pair. The order of the product is determined first by {@code T}
     * (most significant factor) and then by {@code U}.
     * <p>
     * Example:
     * If {@code ts = ["A", "B"]} and {@code us = [1, 2]}, and the constructor concatenates the elements,
     * the method will return a Stream with elements {@code ["A1", "A2", "B1", "B2"]}.
     *
     * @param <T>         Element type for the first collection
     * @param <U>         Element type for the second collection
     * @param <R>         Return type after applying the constructor to each tuple
     * @param ts          The first collection of elements (non-null)
     * @param us          The second collection of elements (non-null)
     * @param constructor A BiFunction to be applied to all pairs, creating the result type {@code R} (non-null)
     * @return A Stream representing the Cartesian product of the given elements after applying the constructor
     * @throws NullPointerException if any of the provided parameters are {@code null}
     */
    public static <T, U, R> Stream<R> of(@NotNull final Collection<T> ts,
                                         @NotNull final Collection<U> us,
                                         @NotNull final BiFunction<? super T, ? super U, ? extends R> constructor) {
        // Ensure that the provided collections and constructor are not null
        requireNonNull(ts, "The first collection (ts) must not be null");
        requireNonNull(us, "The second collection (us) must not be null");
        requireNonNull(constructor, "The constructor function must not be null");

        // Utilize a helper method from ProductUtil to generate the Cartesian product and return as a stream
        return ProductUtil.of(ts, us, constructor);
    }

    /**
     * Creates and returns the cartesian product of the given elements by applying a default
     * Product2 constructor to each tuple.
     * <p>
     * The cartesian product is the combination of all possible pairs between the first stream {@code ts} and the second stream {@code us}.
     * The order of the product is by {@code T} (most significant factor) and then {@code U}.
     * <p>
     * This method applies a default constructor {@code ProductUtil.Product2Impl::new} to create objects of type {@code Product2<T, U>}
     * representing each tuple in the product.
     *
     * @param <T> element type for the first factor order
     * @param <U> element type for the second factor order
     * @param ts  the first factor order (non-null)
     * @param us  the second factor order (non-null)
     * @return a Stream of {@code Product2<T, U>} representing the cartesian product of the given elements
     * @throws NullPointerException if any of the provided parameters are {@code null}.
     */
    public static <T, U> Stream<Product2<T, U>> of(@NotNull final Stream<T> ts,
                                                   @NotNull final Stream<U> us) {
        requireNonNull(ts, "The first stream (ts) must not be null");
        requireNonNull(us, "The second stream (us) must not be null");
        return of(ts, us, ProductUtil.Product2Impl::new);
    }

    /**
     * Creates and returns the cartesian product of the given elements by applying the provided
     * {@code constructor} to each tuple.
     * <p>
     * Similar to the previous method, this function creates pairs of all possible combinations
     * between two streams, {@code ts} and {@code us}, and then applies the provided {@code constructor}
     * to each pair to create objects of type {@code R}.
     * <p>
     * The order of the product is determined first by {@code T} (most significant factor) and then by {@code U}.
     * The provided stream {@code ts} is consumed lazily, and the other provided streams are not.
     *
     * @param <T>         Element type for the first stream
     * @param <U>         Element type for the second stream
     * @param <R>         Return type after applying the constructor to each tuple
     * @param ts          The first stream of elements (non-null)
     * @param us          The second stream of elements (non-null)
     * @param constructor A BiFunction to be applied to all pairs, creating the result type {@code R} (non-null)
     * @return A Stream representing the Cartesian product of the given elements after applying the constructor
     * @throws NullPointerException if any of the provided parameters are {@code null}
     */
    public static <T, U, R> Stream<R> of(@NotNull final Stream<T> ts,
                                         @NotNull final Stream<U> us,
                                         @NotNull final BiFunction<? super T, ? super U, ? extends R> constructor) {
        requireNonNull(ts, "The first stream (ts) must not be null");
        requireNonNull(us, "The second stream (us) must not be null");
        requireNonNull(constructor, "The constructor function must not be null");
        return ProductUtil.of(ts, us, constructor);
    }

    /**
     * Creates and returns the cartesian product of the given elements by applying a default
     * Product3 constructor to each tuple.
     * <p>
     * The cartesian product is formed by creating all possible combinations of the three
     * collections {@code ts}, {@code us}, and {@code vs}, with the order being determined first
     * by {@code T} (most significant factor), then by {@code U}, and finally by {@code V}.
     * <p>
     * The default constructor {@code ProductUtil.Product3Impl::new} is used to create objects of type
     * {@code Product3<T, U, V>} representing each tuple in the product.
     *
     * @param <T> element type for the first factor order
     * @param <U> element type for the second factor order
     * @param <V> element type for the third factor order
     * @param ts  the first factor order (non-null)
     * @param us  the second factor order (non-null)
     * @param vs  the third factor order (non-null)
     * @return a Stream of {@code Product3<T, U, V>} representing the cartesian product of the given elements
     * @throws NullPointerException if any of the provided parameters are {@code null}.
     */
    public static <T, U, V> Stream<Product3<T, U, V>> of(@NotNull final Collection<T> ts,
                                                         @NotNull final Collection<U> us,
                                                         @NotNull final Collection<V> vs) {
        requireNonNull(ts, "The first collection (ts) must not be null");
        requireNonNull(us, "The second collection (us) must not be null");
        requireNonNull(vs, "The third collection (vs) must not be null");
        return of(ts, us, vs, ProductUtil.Product3Impl::new);
    }

    /**
     * Creates and returns the cartesian product of the given elements by applying the provided
     * {@code constructor} to each tuple.
     * <p>
     * Similar to the previous method, this function creates all possible combinations between
     * the three collections {@code ts}, {@code us}, and {@code vs}, and then applies the provided
     * {@code constructor} to each tuple to create objects of type {@code R}.
     * <p>
     * The order of the product is determined first by {@code T} (most significant factor), then
     * by {@code U}, and finally by {@code V}.
     *
     * @param <T>         Element type for the first collection
     * @param <U>         Element type for the second collection
     * @param <V>         Element type for the third collection
     * @param <R>         Return type after applying the constructor to each tuple
     * @param ts          The first collection of elements (non-null)
     * @param us          The second collection of elements (non-null)
     * @param vs          The third collection of elements (non-null)
     * @param constructor A TriFunction to be applied to all tuples, creating the result type {@code R} (non-null)
     * @return A Stream representing the Cartesian product of the given elements after applying the constructor
     * @throws NullPointerException if any of the provided parameters are {@code null}
     */
    public static <T, U, V, R> Stream<R> of(@NotNull final Collection<T> ts,
                                            @NotNull final Collection<U> us,
                                            @NotNull final Collection<V> vs,
                                            @NotNull final TriFunction<T, U, V, R> constructor) {
        requireNonNull(ts, "The first collection (ts) must not be null");
        requireNonNull(us, "The second collection (us) must not be null");
        requireNonNull(vs, "The third collection (vs) must not be null");
        requireNonNull(constructor, "The constructor function must not be null");
        return ProductUtil.of(ts, us, vs, constructor);
    }

    /**
     * Creates and returns the cartesian product of the given elements by applying a default
     * Product3 constructor to each tuple.
     * <p>
     * The order of the product is by T (most significant factor) and then U and then V. The provided stream
     * {@code ts} is consumed lazily, and the other provided streams are not, which allows for more efficient
     * processing of large or infinite streams.
     * <p>
     * The default constructor {@code ProductUtil.Product3Impl::new} is used to represent each tuple in the product.
     *
     * @param <T> element type for the first factor order
     * @param <U> element type for the second factor order
     * @param <V> element type for the third factor order
     * @param ts  the first factor order (non-null)
     * @param us  the second factor order (non-null)
     * @param vs  the third factor order (non-null)
     * @return a Stream of {@code Product3<T, U, V>} representing the cartesian product of the given elements
     * @throws NullPointerException if any of the provided parameters are {@code null}.
     */
    public static <T, U, V> Stream<Product3<T, U, V>> of(@NotNull final Stream<T> ts,
                                                         @NotNull final Stream<U> us,
                                                         @NotNull final Stream<V> vs) {
        requireNonNull(ts, "The first stream (ts) must not be null");
        requireNonNull(us, "The second stream (us) must not be null");
        requireNonNull(vs, "The third stream (vs) must not be null");
        return of(ts, us, vs, ProductUtil.Product3Impl::new);
    }

    /**
     * Creates and returns the cartesian product of the given elements by applying the provided
     * {@code constructor} to each tuple.
     * <p>
     * This function creates all possible combinations between the three streams {@code ts}, {@code us},
     * and {@code vs}, and then applies the provided {@code constructor} to each tuple to create objects
     * of type {@code R}.
     * <p>
     * The order of the product is determined first by {@code T} (most significant factor), then
     * by {@code U}, and finally by {@code V}. The stream {@code ts} is consumed lazily, while the
     * others are not.
     *
     * @param <T>         Element type for the first stream
     * @param <U>         Element type for the second stream
     * @param <V>         Element type for the third stream
     * @param <R>         Return type after applying the constructor to each tuple
     * @param ts          The first stream of elements (non-null)
     * @param us          The second stream of elements (non-null)
     * @param vs          The third stream of elements (non-null)
     * @param constructor A TriFunction to be applied to all tuples, creating the result type {@code R} (non-null)
     * @return A Stream representing the cartesian product of the given elements after applying the constructor
     * @throws NullPointerException if any of the provided parameters are {@code null}
     */
    public static <T, U, V, R> Stream<R> of(@NotNull final Stream<T> ts,
                                            @NotNull final Stream<U> us,
                                            @NotNull final Stream<V> vs,
                                            @NotNull final TriFunction<T, U, V, R> constructor) {
        requireNonNull(ts, "The first stream (ts) must not be null");
        requireNonNull(us, "The second stream (us) must not be null");
        requireNonNull(vs, "The third stream (vs) must not be null");
        requireNonNull(constructor, "The constructor function must not be null");
        return ProductUtil.of(ts, us, vs, constructor);
    }


    /**
     * Function interface representing a function that accepts three arguments and produces a result.
     *
     * @param <T> Type of the first argument
     * @param <U> Type of the second argument
     * @param <V> Type of the third argument
     * @param <R> Type of the result
     */
    @FunctionalInterface
    public interface TriFunction<T, U, V, R> {
        /**
         * Applies this function to the given arguments, creating a result of type {@code R}.
         * <p>
         * This functional interface is used to apply custom behavior to tuples within the cartesian
         * product, allowing for flexible creation of objects to represent those tuples.
         *
         * @param t the first function argument
         * @param u the second function argument
         * @param v the third function argument
         * @return the function result, which can be of any type {@code R}
         */
        R apply(T t, U u, V v);
    }


    public interface HasFirst<T> {
        /**
         * Returns the first component.
         *
         * @return the first component
         */
        T first();
    }

    /**
     * Interface representing an object that has a second component of type {@code U}.
     *
     * @param <U> Type of the second component
     */
    public interface HasSecond<U> {
        /**
         * Returns the second component.
         *
         * @return the second component
         */
        U second();
    }

    /**
     * Interface representing an object that has a third component of type {@code V}.
     *
     * @param <V> Type of the third component
     */
    public interface HasThird<V> {
        /**
         * Returns the third component.
         *
         * @return the third component
         */
        V third();
    }

    /**
     * A Product2 is a composite object comprising two components.
     * It's a part of a tuple-like structure with two elements.
     *
     * @param <T> Type of the first component
     * @param <U> Type of the second component
     */
    public interface Product2<T, U> extends HasFirst<T>, HasSecond<U> {

    }

    /**
     * A Product3 is a composite object comprising three components.
     * It's a part of a tuple-like structure with three elements.
     *
     * @param <T> Type of the first component
     * @param <U> Type of the second component
     * @param <V> Type of the third component
     */
    public interface Product3<T, U, V> extends HasFirst<T>, HasSecond<U>, HasThird<V> {
    }

}