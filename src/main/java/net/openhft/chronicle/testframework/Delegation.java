package net.openhft.chronicle.testframework;

import net.openhft.chronicle.testframework.internal.DelegationBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * Utility class to build delegator instances that forward method invocations
 * to a specified delegate object. This class facilitates a fluent API for customizing
 * the behaviour of the delegator. The builder creates the delegator as a
 * dynamic proxy using {@link java.lang.reflect.Proxy}.
 * <p>
 * This class cannot be instantiated.
 */
public final class Delegation {

    // Suppresses default constructor, ensuring non-instantiability.
    private Delegation() {
    }

    /**
     * Creates and returns a new builder for a delegator instance that will use the provided
     * {@code delegate} as the delegate. Method invocations on the built instance will be delegated to the
     * provided delegate.
     *
     * @param delegate The object to delegate invocations to
     * @param <D>      Provided delegate type
     * @return New delegator builder
     * @throws NullPointerException if the provided delegate is {@code null}.
     */
    public static <D> Builder<Object, D> of(@NotNull final D delegate) {
        requireNonNull(delegate);
        return new DelegationBuilder<>(delegate);
    }

    /**
     * Interface for building a delegation object. Allows customisation of the
     * type view and the {@code toString()} method of the delegate. The instance
     * produced by {@link #build()} is a dynamic proxy.
     *
     * @param <T> Target type
     * @param <D> Delegation type
     */
    public interface Builder<T, D> {

        // Future: Add capability to override any method using T::method references

        /**
         * Specifies the interface the delegate should be viewed as.
         * <p>
         * The default view is {@link Object}.
         *
         * @param type the interface the proxy should implement, non-null
         * @param <N>  the new view type
         * @return this builder
         */
        <N extends D> Builder<N, D> as(Class<N> type);

        /**
         * Defines the function used when {@code toString()} is invoked on the
         * proxy. The default is the delegate's {@link Object#toString()}.
         *
         * @param toStringFunction function applied to the delegate, non-null
         * @return this builder
         */
        Builder<T, D> toStringFunction(Function<? super D, String> toStringFunction);

        /**
         * Builds and returns the configured proxy.
         *
         * @return a dynamic proxy that forwards calls to the delegate
         */
        T build();
    }
}
