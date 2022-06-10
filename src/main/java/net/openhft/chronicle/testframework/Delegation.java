package net.openhft.chronicle.testframework;

import net.openhft.chronicle.testframework.internal.DelegationBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public final class Delegation {

    // Suppresses default constructor, ensuring non-instantiability.
    private Delegation() {
    }

    /**
     * Creates and returns a new builder for a delegator instance that is using the provided
     * {@code delegate} as a delegate. I.e. methods invoked on a built instance will be delegated to the
     * provided delegate.
     *
     * @param delegate to delegate invocations to
     * @param <D>      provided delegate type
     * @return new delegator builder
     * @throws NullPointerException if any provided parameter is {@code null}.
     */
    public static <D> Builder<Object, D> of(@NotNull final D delegate) {
        requireNonNull(delegate);
        return new DelegationBuilder<>(delegate);
    }

    /**
     * The new instance will default it's {@link Object#toString()} method to the one of the
     * provided delegate.
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
         * @param type to view the delegate as (non-null)
         * @param <N>  the new type of how the delegate should be viewed
         * @return this builder
         */
        <N extends D> Builder<N, D> as(Class<N> type);

        /**
         * Specifies the {@code tostring()) the view should use.
         * <p>
         * The default view is {@link Object#toString()}  }.
         *
         * @param toStringFunction to be applied to the delegate (non-null)
         * @return this builder
         */
        Builder<T, D> toStringFunction(Function<? super D, String> toStringFunction);

        /**
         * Creates and returns a new view of type T of the underlying delegate of type D
         *
         * @return a new view
         */
        T build();
    }

}