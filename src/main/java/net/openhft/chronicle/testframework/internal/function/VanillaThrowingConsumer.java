package net.openhft.chronicle.testframework.internal.function;

import net.openhft.chronicle.testframework.function.ThrowingConsumer;
import net.openhft.chronicle.testframework.function.ThrowingConsumerException;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public final class VanillaThrowingConsumer<T> implements Consumer<T> {

    private final ThrowingConsumer<T, ?> delegate;

    public VanillaThrowingConsumer(@NotNull final ThrowingConsumer<T, ?> delegate) {
        this.delegate = requireNonNull(delegate);
    }

    @Override
    public void accept(T t) {
        try {
            delegate.accept(t);
        } catch (Exception e) {
            throw new ThrowingConsumerException(e);
        }
    }

}