package net.openhft.chronicle.testframework.internal;

import net.openhft.chronicle.testframework.Product;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public final class ProductUtil {

    private ProductUtil() {
    }

    public static <T, U, R> Stream<R> of(Collection<T> ts,
                                         Collection<U> us,
                                         BiFunction<? super T, ? super U, ? extends R> constructor) {
        requireNonNull(ts);
        requireNonNull(us);
        requireNonNull(constructor);
        return ts.stream()
                .flatMap(t -> us.stream()
                        .map(u -> constructor.apply(t, u)));
    }

    public static <T, U, R> Stream<R> of(Stream<T> ts,
                                         Stream<U> us,
                                         BiFunction<? super T, ? super U, ? extends R> constructor) {
        requireNonNull(ts);
        requireNonNull(us);
        requireNonNull(constructor);
        final List<U> innerU = us.collect(Collectors.toList());
        return ts
                .flatMap(t -> innerU.stream()
                        .map(u -> constructor.apply(t, u)));
    }

    public static <T, U, V, R> Stream<R> of(Collection<T> ts,
                                            Collection<U> us,
                                            Collection<V> vs,
                                            Product.TriFunction<? super T, ? super U, ? super V, ? extends R> constructor) {
        requireNonNull(ts);
        requireNonNull(us);
        requireNonNull(vs);
        requireNonNull(constructor);
        return ts.stream()
                .flatMap(t -> us.stream()
                        .flatMap((u -> vs.stream()
                                .map(v -> constructor.apply(t, u, v)))));
    }

    public static <T, U, V, R> Stream<R> of(Stream<T> ts,
                                            Stream<U> us,
                                            Stream<V> vs,
                                            Product.TriFunction<? super T, ? super U, ? super V, ? extends R> constructor) {
        requireNonNull(ts);
        requireNonNull(us);
        requireNonNull(vs);
        requireNonNull(constructor);
        final List<U> innerU = us.collect(Collectors.toList());
        final List<V> innerV = vs.collect(Collectors.toList());
        return ts
                .flatMap(t -> innerU.stream()
                        .flatMap((u -> innerV.stream()
                                .map(v -> constructor.apply(t, u, v)))));
    }


    public static final class Product2Impl<T, U> implements Product.Product2<T, U> {

        private final T first;
        private final U second;

        public Product2Impl(T first, U second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public T first() {
            return first;
        }

        @Override
        public U second() {
            return second;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Product2Impl<?, ?> product2 = (Product2Impl<?, ?>) o;

            if (!Objects.equals(first, product2.first)) return false;
            return Objects.equals(second, product2.second);
        }

        @Override
        public int hashCode() {
            int result = first != null ? first.hashCode() : 0;
            result = 31 * result + (second != null ? second.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Product2Impl{" +
                    "first=" + first +
                    ", second=" + second +
                    '}';
        }
    }


    public static final class Product3Impl<T, U, V> implements Product.Product3<T, U, V> {

        private final T first;
        private final U second;
        private final V third;


        public Product3Impl(T first, U second, V third) {
            this.first = first;
            this.second = second;
            this.third = third;
        }

        @Override
        public T first() {
            return first;
        }

        @Override
        public U second() {
            return second;
        }

        @Override
        public V third() {
            return third;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Product3Impl<?, ?, ?> product3 = (Product3Impl<?, ?, ?>) o;

            if (!Objects.equals(first, product3.first)) return false;
            if (!Objects.equals(second, product3.second)) return false;
            return Objects.equals(third, product3.third);
        }

        @Override
        public int hashCode() {
            int result = first != null ? first.hashCode() : 0;
            result = 31 * result + (second != null ? second.hashCode() : 0);
            result = 31 * result + (third != null ? third.hashCode() : 0);
            return result;
        }


        @Override
        public String toString() {
            return "Product3Impl{" +
                    "first=" + first +
                    ", second=" + second +
                    ", third=" + third +
                    '}';
        }
    }


}
