package net.openhft.chronicle.testframework;

import net.openhft.chronicle.testframework.internal.DelegationBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public final class Delegation {

    // Suppresses default constructor, ensuring non-instantiability.
    private Delegation() {}

    /**
     * Creates and returns a new builder for a delegator instance of the provided {@code type} that is using the provided
     * {@code delegate} as a delegator. I.e. methods invoked on the new instance will be delegated to the
     * provided delegate. The new instance will default it's {@link Object#toString()} method to the one of the
     * provided delegate.
     *
     * @param delegate to delegate invocations to
     * @param type     of returned delegator
     * @param <D>      provided delegate type
     * @param <T>      returned delegator type
     * @return new delegator builder
     * @throws NullPointerException if any provided parameter is {@code null}.
     */
    public static <T extends D, D> Builder<T, D> of(@NotNull final Class<T> type,
                                                    @NotNull final D delegate) {
        requireNonNull(type);
        requireNonNull(delegate);
        return new DelegationBuilder<>(type, delegate);
    }

    public interface Builder<T, D> {

        // Future: Add capability to override any method using T::method references

        Builder<T, D> toStringFunction(Function<? super D, String> toStringFunction);

        T build();
    }

}