package net.openhft.chronicle.testframework.internal.function;

import net.openhft.chronicle.testframework.function.NamedConsumer;

import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public final class VanillaNamedConsumer<T> implements NamedConsumer<T> {

    private final Consumer<T> consumer;
    private final String name;

    public VanillaNamedConsumer(final Consumer<T> consumer,
                                final String name) {
        this.consumer = requireNonNull(consumer);
        this.name = requireNonNull(name);
    }

    @Override
    public void accept(T t) {
        consumer.accept(t);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}