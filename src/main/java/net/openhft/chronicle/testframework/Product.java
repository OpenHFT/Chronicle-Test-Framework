package net.openhft.chronicle.testframework;

import net.openhft.chronicle.testframework.internal.ProductUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Utilities for building Cartesian products of collections or streams.
 * <p>
 * Each {@code of} method emits a {@link Stream} containing every
 * combination of the supplied elements. Any empty input yields an empty
 * stream and a {@code NullPointerException} is thrown for {@code null}
 * arguments.
 * <p>
 * Example:
 * <pre>
 * List&lt;String&gt; ts = List.of("A", "B");
 * List&lt;Integer&gt; us = List.of(1, 2);
 * Product.of(ts, us)
 *         .forEach(p -&gt; System.out.println(p.first() + ", " + p.second()));
 * </pre>
 */
public final class Product {

    // Suppresses default constructor, ensuring non-instantiability.
    private Product() {
    }

    /**
     * Returns a stream of pairs formed from {@code ts} and {@code us}.
     * <p>
     * Edge cases:
     * <ul>
     * <li>An empty input yields an empty stream.</li>
     * <li>{@code null} arguments cause a {@link NullPointerException}.</li>
     * </ul>
     * Example:
     * <pre>
     * Product.of(List.of("A"), List.of(1))
     *         .forEach(p -&gt; System.out.println(p.first() + ":" + p.second()));
     * </pre>
     *
     * @param <T> element type for the first collection
     * @param <U> element type for the second collection
     * @param ts  first collection, not {@code null}
     * @param us  second collection, not {@code null}
     * @return the Cartesian product as a {@link Stream} of {@link Product2}
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
     * Returns a stream created by applying {@code constructor} to all pairs
     * from {@code ts} and {@code us}.
     * <p>
     * Edge cases:
     * <ul>
     * <li>Empty inputs produce an empty stream.</li>
     * <li>{@code null} arguments cause a {@link NullPointerException}.</li>
     * </ul>
     * Example:
     * <pre>
     * Product.of(List.of("A"), List.of(1), (a, b) -&gt; a + b)
     *         .forEach(System.out::println);
     * </pre>
     *
     * @param <T>         element type for the first collection
     * @param <U>         element type for the second collection
     * @param <R>         result type after applying the constructor
     * @param ts          first collection, not {@code null}
     * @param us          second collection, not {@code null}
     * @param constructor function applied to each pair, not {@code null}
     * @return stream of constructed results
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
     * Returns a stream of pairs from {@code ts} and {@code us} using the
     * default {@link Product2} implementation.
     * <p>
     * Edge cases:
     * <ul>
     * <li>An empty input yields an empty stream.</li>
     * <li>{@code null} arguments cause a {@link NullPointerException}.</li>
     * </ul>
     * Example:
     * <pre>
     * Product.of(Stream.of("A"), Stream.of(1))
     *         .forEach(p -&gt; System.out.println(p.first() + ":" + p.second()));
     * </pre>
     *
     * @param <T> element type for the first factor
     * @param <U> element type for the second factor
     * @param ts  first stream, not {@code null}
     * @param us  second stream, not {@code null}
     * @return stream of {@code Product2} pairs
     */
    public static <T, U> Stream<Product2<T, U>> of(@NotNull final Stream<T> ts,
                                                   @NotNull final Stream<U> us) {
        requireNonNull(ts, "The first stream (ts) must not be null");
        requireNonNull(us, "The second stream (us) must not be null");
        return of(ts, us, ProductUtil.Product2Impl::new);
    }

    /**
     * Returns a stream created by applying {@code constructor} to each pair
     * from the two streams.
     * <p>
     * Edge cases:
     * <ul>
     * <li>Empty inputs produce an empty stream.</li>
     * <li>{@code null} arguments cause a {@link NullPointerException}.</li>
     * </ul>
     * Example:
     * <pre>
     * Product.of(Stream.of("A"), Stream.of(1), (a, b) -&gt; a + b)
     *         .forEach(System.out::println);
     * </pre>
     *
     * @param <T>         element type for the first stream
     * @param <U>         element type for the second stream
     * @param <R>         result type after applying the constructor
     * @param ts          first stream, not {@code null}
     * @param us          second stream, not {@code null}
     * @param constructor function applied to each pair, not {@code null}
     * @return stream of constructed results
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
     * Returns triples from {@code ts}, {@code us} and {@code vs} using the
     * default {@link Product3} implementation.
     * <p>
     * Edge cases:
     * <ul>
     * <li>An empty input yields an empty stream.</li>
     * <li>{@code null} arguments cause a {@link NullPointerException}.</li>
     * </ul>
     * Example:
     * <pre>
     * Product.of(List.of("A"), List.of(1), List.of(true))
     *         .forEach(p -&gt; System.out.println(p.first() + ":" + p.second() + ":" + p.third()));
     * </pre>
     *
     * @param <T> element type for the first factor
     * @param <U> element type for the second factor
     * @param <V> element type for the third factor
     * @param ts  first collection, not {@code null}
     * @param us  second collection, not {@code null}
     * @param vs  third collection, not {@code null}
     * @return stream of {@code Product3} triples
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
     * Returns triples by applying {@code constructor} to each combination of
     * {@code ts}, {@code us} and {@code vs}.
     * <p>
     * Edge cases:
     * <ul>
     * <li>An empty input yields an empty stream.</li>
     * <li>{@code null} arguments cause a {@link NullPointerException}.</li>
     * </ul>
     * Example:
     * <pre>
     * Product.of(List.of("A"), List.of(1), List.of(true), (a, b, c) -&gt; a + b + c)
     *         .forEach(System.out::println);
     * </pre>
     *
     * @param <T>         element type for the first collection
     * @param <U>         element type for the second collection
     * @param <V>         element type for the third collection
     * @param <R>         result type after applying the constructor
     * @param ts          first collection, not {@code null}
     * @param us          second collection, not {@code null}
     * @param vs          third collection, not {@code null}
     * @param constructor function applied to each triple, not {@code null}
     * @return stream of constructed results
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
     * Returns a stream of triples from three streams using the default
     * {@link Product3} implementation.
     * <p>
     * Edge cases:
     * <ul>
     * <li>An empty input yields an empty stream.</li>
     * <li>{@code null} arguments cause a {@link NullPointerException}.</li>
     * </ul>
     * Example:
     * <pre>
     * Product.of(Stream.of("A"), Stream.of(1), Stream.of(true))
     *         .forEach(p -&gt; System.out.println(p.first() + ":" + p.second() + ":" + p.third()));
     * </pre>
     *
     * @param <T> element type for the first factor
     * @param <U> element type for the second factor
     * @param <V> element type for the third factor
     * @param ts  first stream, not {@code null}
     * @param us  second stream, not {@code null}
     * @param vs  third stream, not {@code null}
     * @return stream of {@code Product3} triples
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
     * Returns triples produced by applying {@code constructor} to every
     * combination of the three streams.
     * <p>
     * Edge cases:
     * <ul>
     * <li>An empty input yields an empty stream.</li>
     * <li>{@code null} arguments cause a {@link NullPointerException}.</li>
     * </ul>
     * Example:
     * <pre>
     * Product.of(Stream.of("A"), Stream.of(1), Stream.of(true), (a, b, c) -&gt; a + b + c)
     *         .forEach(System.out::println);
     * </pre>
     *
     * @param <T>         element type for the first stream
     * @param <U>         element type for the second stream
     * @param <V>         element type for the third stream
     * @param <R>         result type after applying the constructor
     * @param ts          first stream, not {@code null}
     * @param us          second stream, not {@code null}
     * @param vs          third stream, not {@code null}
     * @param constructor function applied to each triple, not {@code null}
     * @return stream of constructed results
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

    /**
     * An interface representing an object that has a first component.
     * This can be used to add functionality to objects that are made up of multiple components.
     *
     * @param <T> The type of the first component.
     */
    public interface HasFirst<T> {

        /**
         * Returns the first component of the object.
         * This method should be implemented to return the first component of the multi-component object.
         *
         * @return The first component of the object.
         */
        T first();
    }

    /**
     * An interface representing an object that has a second component.
     * This can be used to add functionality to objects that are made up of multiple components.
     *
     * @param <U> The type of the second component.
     */
    public interface HasSecond<U> {

        /**
         * Returns the second component of the object.
         * This method should be implemented to return the second component of the multi-component object.
         *
         * @return The second component of the object.
         */
        U second();
    }
    /**
     * Interface representing an object that has a third component of type {@code V}.
     * This can be used to add functionality to objects that are made up of three components.
     *
     * @param <V> Type of the third component
     */
    public interface HasThird<V> {

        /**
         * Returns the third component of the object.
         * This method should be implemented to return the third component of the multi-component object.
         *
         * @return The third component of the object.
         */
        V third();
    }

    /**
     * A Product2 is a composite object comprising two components.
     * It's a part of a tuple-like structure with two elements, extending the HasFirst and HasSecond interfaces.
     * This provides a way to group two related objects together into a single unit.
     *
     * @param <T> Type of the first component
     * @param <U> Type of the second component
     */
    public interface Product2<T, U> extends HasFirst<T>, HasSecond<U> {

        // Interface doesn't have any additional methods or fields, but inherits those from HasFirst and HasSecond.
    }

    /**
     * A Product3 is a composite object comprising three components.
     * It's a part of a tuple-like structure with three elements, extending the HasFirst, HasSecond, and HasThird interfaces.
     * This provides a way to group three related objects together into a single unit.
     *
     * @param <T> Type of the first component
     * @param <U> Type of the second component
     * @param <V> Type of the third component
     */
    public interface Product3<T, U, V> extends HasFirst<T>, HasSecond<U>, HasThird<V> {

        // Interface doesn't have any additional methods or fields, but inherits those from HasFirst, HasSecond, and HasThird.
    }
}
