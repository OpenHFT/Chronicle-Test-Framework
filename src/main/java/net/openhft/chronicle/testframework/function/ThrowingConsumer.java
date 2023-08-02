package net.openhft.chronicle.testframework.function;

import net.openhft.chronicle.testframework.internal.function.VanillaThrowingConsumer;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * Represents an operation that accepts a single input argument and returns no
 * result and that can throw an exception of type {@code X}. Unlike most other functional interfaces,
 * {@code ThrowingConsumer} is expected to operate via side-effects and may throw exceptions during execution.
 * <p>
 * This is a functional interface whose functional method is {@link #accept(Object)}.
 *
 * @param <T> the type of the input to the operation
 * @param <X> the type of the exception that can be thrown by this operation
 */
@FunctionalInterface
public interface ThrowingConsumer<T, X extends Exception> {

    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     * @throws X if an exception of type {@code X} occurs during execution
     */
    void accept(T t) throws X;

    /**
     * Creates and returns a new Consumer that will wrap any exceptions thrown by the
     * provided {@code throwingConsumer} in a {@link ThrowingConsumerException}.
     * <p>
     * This method allows the integration of a ThrowingConsumer into contexts where a regular
     * Consumer is expected, by translating checked exceptions into runtime exceptions.
     *
     * @param throwingConsumer the ThrowingConsumer to wrap, must not be null
     * @param <T>              consumed type
     * @return a wrapped Consumer that translates checked exceptions into runtime exceptions
     * @throws NullPointerException if {@code throwingConsumer} is null
     */
    @NotNull
    static <T> Consumer<T> of(@NotNull final ThrowingConsumer<T, ?> throwingConsumer) {
        requireNonNull(throwingConsumer);
        return new VanillaThrowingConsumer<>(throwingConsumer);
    }

}
