package net.openhft.chronicle.testframework;

import net.openhft.chronicle.testframework.internal.ProductUtil;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public final class Product {

    // Suppresses default constructor, ensuring non-instantiability.
    private Product() {
    }

    public static <T, U> Stream<Product2<T, U>> of(Collection<T> ts,
                                                   Collection<U> us) {
        requireNonNull(ts);
        requireNonNull(us);
        return of(ts, us, ProductUtil.Product2Impl::new);
    }


    public static <T, U, R> Stream<R> of(Collection<T> ts,
                                         Collection<U> us,
                                         BiFunction<? super T, ? super U, ? extends R> constructor) {
        requireNonNull(ts);
        requireNonNull(us);
        requireNonNull(constructor);
        return ProductUtil.of(ts, us, constructor);
    }


    public static <T, U, V> Stream<Product3<T, U, V>> of(Collection<T> ts,
                                                         Collection<U> us,
                                                         Collection<V> vs) {
        requireNonNull(ts);
        requireNonNull(us);
        requireNonNull(vs);
        return of(ts, us, vs, ProductUtil.Product3Impl::new);
    }

    public static <T, U, V, R> Stream<R> of(Collection<T> ts,
                                            Collection<U> us,
                                            Collection<V> vs,
                                            TriFunction<T, U, V, R> constructor) {
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
