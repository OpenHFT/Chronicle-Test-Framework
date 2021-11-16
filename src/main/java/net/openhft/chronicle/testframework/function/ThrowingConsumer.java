package net.openhft.chronicle.testframework.function;

import net.openhft.chronicle.testframework.internal.VanillaThrowingConsumer;

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

    static <T> Consumer<T> of(final ThrowingConsumer<T, ?> consumer) {
        requireNonNull(consumer);
        return new VanillaThrowingConsumer<>(consumer);
    }

}
