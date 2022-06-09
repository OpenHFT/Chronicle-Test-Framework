package net.openhft.chronicle.testframework.internal.function;

import net.openhft.chronicle.testframework.function.NamedConsumer;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public final class VanillaNamedConsumer<T> implements NamedConsumer<T> {

    private final Consumer<T> consumer;
    private final String name;

    public VanillaNamedConsumer(@NotNull final Consumer<T> consumer,
                                @NotNull final String name) {
        this.consumer = requireNonNull(consumer);
        this.name = requireNonNull(name);
    }

    @Override
    public void accept(T t) {
        consumer.accept(t);
    }

    @Override
    public @NotNull String name() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}