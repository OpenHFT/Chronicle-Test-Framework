package net.openhft.chronicle.testframework.function;

import net.openhft.chronicle.testframework.internal.function.VanillaThrowingConsumer;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * Represents an operation that accepts a single input argument and returns no
 * result and that can throw an Exception. Unlike most other functional interfaces, {@code Consumer} is expected
 * to operate via side-effects.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #accept(Object)}.
 *
 * @param <T> the type of the input to the operation
 */
@FunctionalInterface
public interface ThrowingConsumer<T, X extends Exception> {

    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     * @throws X if an exception occurs
     */
    void accept(T t) throws X;

    /**
     * Creates and returns a new Consumer that will wrap any exceptions thrown by the
     * provided {@code throwingConsumer} in a {@link ThrowingConsumerException}
     * @param throwingConsumer to wrap (non-null)
     * @param <T> consumed type
     * @return a wrapped Consumer
     */
    @NotNull
    static <T> Consumer<T> of(@NotNull final ThrowingConsumer<T, ?> throwingConsumer) {
        requireNonNull(throwingConsumer);
        return new VanillaThrowingConsumer<>(throwingConsumer);
    }

}
