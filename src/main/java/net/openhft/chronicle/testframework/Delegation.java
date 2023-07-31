package net.openhft.chronicle.testframework;

import net.openhft.chronicle.testframework.internal.DelegationBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * Utility class to build delegator instances that forward method invocations
 * to a specified delegate object. This class facilitates a fluent API for customizing
 * the behavior of the delegator.
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
     * Interface for building a delegation object. Allows customization of the type view
     * and the {@code toString()} method of the delegate.
     *
     * @param <T> Target type
     * @param <D> Delegation type
     */
    public interface Builder<T, D> {

        // Future: Add capability to override any method using T::method references

        /**
         * Specifies the type the delegate should be viewed as.
         * <p>
         * The default view is {@link Object }.
         *
         * @param type The class to view the delegate as (non-null)
         * @param <N>  The new type of how the delegate should be viewed
         * @return This builder, for chaining
         */
        <N extends D> Builder<N, D> as(Class<N> type);

        /**
         * Specifies the {@code toString()} function the view should use.
         * <p>
         * The default view is {@link Object#toString()}.
         *
         * @param toStringFunction The function to be applied to the delegate (non-null)
         * @return This builder, for chaining
         */
        Builder<T, D> toStringFunction(Function<? super D, String> toStringFunction);

        /**
         * Creates and returns a new view of type T of the underlying delegate of type D.
         * <p>
         * This method finalizes the builder and returns the configured delegator.
         *
         * @return A new view of the delegate
         */
        T build();
    }
}
