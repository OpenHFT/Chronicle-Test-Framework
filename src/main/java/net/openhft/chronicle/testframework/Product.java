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
     * Creates and returns the cartesian product of the given elements by applying a default
     * Product2 constructor to each tuple.
     * <p>
     * The order of the product is by T (most significant factor) and then U.
     *
     * @param <T> element type for the first factor order
     * @param <U> element type for the second factor order
     * @param ts the first factor order (non-null)
     * @param us the second factor order (non-null)
     * @return the cartesian product of the given elements
     * @throws NullPointerException if any of the provided parameters are {@code null}.
     */
    public static <T, U> Stream<Product2<T, U>> of(@NotNull final Collection<T> ts,
                                                   @NotNull final Collection<U> us) {
        requireNonNull(ts);
        requireNonNull(us);
        return of(ts, us, ProductUtil.Product2Impl::new);
    }

    /**
     * Creates and returns the cartesian product of the given elements by applying the provided
     * {@code constructors} to each tuple.
     * <p>
     * The order of the product is by T (most significant factor) and then U.
     *
     * @param <T> element type for the first factor order
     * @param <U> element type for the second factor order
     * @param ts the first factor order (non-null)
     * @param us the second factor order (non-null)
     * @param constructor to be applied to all tuples (non-null)
     * @return the cartesian product of the given elements
     * @throws NullPointerException if any of the provided parameters are {@code null}.
     */
    public static <T, U, R> Stream<R> of(@NotNull final Collection<T> ts,
                                         @NotNull final Collection<U> us,
                                         @NotNull final BiFunction<? super T, ? super U, ? extends R> constructor) {
        requireNonNull(ts);
        requireNonNull(us);
        requireNonNull(constructor);
        return ProductUtil.of(ts, us, constructor);
    }

    /**
     * Creates and returns the cartesian product of the given elements by applying the provided
     * {@code constructors} to each tuple.
     * <p>
     * The order of the product is by T (most significant factor) and then U. The provided stream
     * {@code ts} is consumed lazily and the other provided streams are not.
     *
     * @param <T> element type for the first factor order
     * @param <U> element type for the second factor order
     * @param ts the first factor order (non-null)
     * @param us the second factor order (non-null)
     * @return the cartesian product of the given elements
     * @throws NullPointerException if any of the provided parameters are {@code null}.
     */
    public static <T, U> Stream<Product2<T, U>> of(@NotNull final Stream<T> ts,
                                                   @NotNull final Stream<U> us) {
        requireNonNull(ts);
        requireNonNull(us);
        return of(ts, us, ProductUtil.Product2Impl::new);
    }

    /**
     * Creates and returns the cartesian product of the given elements by applying a default
     * Product2 constructor to each tuple.
     * <p>
     * The order of the product is by T (most significant factor) and then U. The provided stream
     * {@code ts} is consumed lazily and the other provided streams are not.
     *
     * @param <T> element type for the first factor order
     * @param <U> element type for the second factor order
     * @param ts the first factor order (non-null)
     * @param us the second factor order (non-null)
     * @param constructor to be applied to all tuples (non-null)
     * @return the cartesian product of the given elements
     * @throws NullPointerException if any of the provided parameters are {@code null}.
     */
    public static <T, U, R> Stream<R> of(@NotNull final Stream<T> ts,
                                         @NotNull final Stream<U> us,
                                         @NotNull final BiFunction<? super T, ? super U, ? extends R> constructor) {
        requireNonNull(ts);
        requireNonNull(us);
        requireNonNull(constructor);
        return ProductUtil.of(ts, us, constructor);
    }

    /**
     * Creates and returns the cartesian product of the given elements by applying a default
     * Product3 constructor to each tuple.
     * <p>
     * The order of the product is by T (most significant factor) and then U and then V.
     *
     * @param <T> element type for the first factor order
     * @param <U> element type for the second factor order
     * @param <V> element type for the third factor order
     * @param ts the first factor order (non-null)
     * @param us the second factor order (non-null)
     * @param vs the third factor order (non-null)
     * @return the cartesian product of the given elements
     * @throws NullPointerException if any of the provided parameters are {@code null}.
     */
    public static <T, U, V> Stream<Product3<T, U, V>> of(@NotNull final Collection<T> ts,
                                                         @NotNull final Collection<U> us,
                                                         @NotNull final Collection<V> vs) {
        requireNonNull(ts);
        requireNonNull(us);
        requireNonNull(vs);
        return of(ts, us, vs, ProductUtil.Product3Impl::new);
    }

    /**
     * Creates and returns the cartesian product of the given elements by applying the provided
     * {@code constructors} to each tuple.
     * <p>
     * The order of the product is by T (most significant factor) and then U and then V.
     *
     * @param <T> element type for the first factor order
     * @param <U> element type for the second factor order
     * @param <V> element type for the third factor order
     * @param ts the first factor order (non-null)
     * @param us the second factor order (non-null)
     * @param vs the third factor order (non-null)
     * @param constructor to be applied to all tuples (non-null)
     * @return the cartesian product of the given elements
     * @throws NullPointerException if any of the provided parameters are {@code null}.
     */
    public static <T, U, V, R> Stream<R> of(@NotNull final Collection<T> ts,
                                            @NotNull final Collection<U> us,
                                            @NotNull final Collection<V> vs,
                                            @NotNull final TriFunction<T, U, V, R> constructor) {
        requireNonNull(ts);
        requireNonNull(us);
        requireNonNull(vs);
        requireNonNull(constructor);
        return ProductUtil.of(ts, us, vs, constructor);
    }

    /**
     * Creates and returns the cartesian product of the given elements by applying a default
     * Product3 constructor to each tuple.
     * <p>
     * The order of the product is by T (most significant factor) and then U and then V. The provided stream
     * {@code ts} is consumed lazily and the other provided streams are not.
     *
     * @param <T> element type for the first factor order
     * @param <U> element type for the second factor order
     * @param <V> element type for the third factor order
     * @param ts the first factor order (non-null)
     * @param us the second factor order (non-null)
     * @param vs the third factor order (non-null)
     * @return the cartesian product of the given elements
     * @throws NullPointerException if any of the provided parameters are {@code null}.
     */
    public static <T, U, V> Stream<Product3<T, U, V>> of(@NotNull final Stream<T> ts,
                                                         @NotNull final Stream<U> us,
                                                         @NotNull final Stream<V> vs) {
        requireNonNull(ts);
        requireNonNull(us);
        requireNonNull(vs);
        return of(ts, us, vs, ProductUtil.Product3Impl::new);
    }

    /**
     * Creates and returns the cartesian product of the given elements by the provided
     * {@code constructors} to each tuple.
     * <p>
     * The order of the product is by T (most significant factor) and then U and then V. The provided stream
     * {@code ts} is consumed lazily and the other provided streams are not.
     *
     * @param <T> element type for the first factor order
     * @param <U> element type for the second factor order
     * @param <V> element type for the third factor order
     * @param ts the first factor order (non-null)
     * @param us the second factor order (non-null)
     * @param vs the third factor order (non-null)
     * @param constructor to be applied to all tuples (non-null)
     * @return the cartesian product of the given elements
     * @throws NullPointerException if any of the provided parameters are {@code null}.
     */
    public static <T, U, V, R> Stream<R> of(@NotNull final Stream<T> ts,
                                            @NotNull final Stream<U> us,
                                            @NotNull final Stream<V> vs,
                                            @NotNull final TriFunction<T, U, V, R> constructor) {
        requireNonNull(ts);
        requireNonNull(us);
        requireNonNull(vs);
        requireNonNull(constructor);
        return ProductUtil.of(ts, us, vs, constructor);
    }

    @FunctionalInterface
    public interface TriFunction<T, U, V, R> {

        /**
         * Applies this function to the given arguments.
         *
         * @param t the first function argument
         * @param u the second function argument
         * @param v the third function argument
         * @return the function result
         */
        R apply(T t, U u, V v);
    }


    public interface HasFirst<T> {
        T first();
    }

    public interface HasSecond<U> {
        U second();
    }

    public interface HasThird<V> {
        V third();
    }

    public interface Product2<T, U> extends HasFirst<T>, HasSecond<U> {
    }

    public interface Product3<T, U, V> extends HasFirst<T>, HasSecond<U>, HasThird<V> {
    }

}