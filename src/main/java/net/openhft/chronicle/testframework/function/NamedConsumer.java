package net.openhft.chronicle.testframework.function;

import net.openhft.chronicle.testframework.internal.function.VanillaNamedConsumer;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public interface NamedConsumer<T> extends HasName, Consumer<T> {
    @NotNull
    static <T> NamedConsumer<T> of(@NotNull final Consumer<T> consumer,
                                   @NotNull final String name) {
        requireNonNull(consumer);
        requireNonNull(name);
        return new VanillaNamedConsumer<>(consumer, name);
    }

    @NotNull
    static <T> NamedConsumer<T> ofThrowing(@NotNull final ThrowingConsumer<T, ?> consumer,
                                           @NotNull final String name) {
        return of(ThrowingConsumer.of(consumer), name);
    }
}
