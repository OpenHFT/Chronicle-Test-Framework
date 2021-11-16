package net.openhft.chronicle.testframework.function;

import net.openhft.chronicle.testframework.internal.VanillaNamedConsumer;

import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public interface NamedConsumer<T> extends HasName, Consumer<T> {

    static <T> NamedConsumer<T> of(final Consumer<T> consumer,
                                   final String name) {
        requireNonNull(consumer);
        requireNonNull(name);
        return new VanillaNamedConsumer<>(consumer, name);
    }

    static <T> NamedConsumer<T> ofThrowing(final ThrowingConsumer<T, ?> consumer,
                                           final String name) {
        return of(ThrowingConsumer.of(consumer), name);
    }
}
