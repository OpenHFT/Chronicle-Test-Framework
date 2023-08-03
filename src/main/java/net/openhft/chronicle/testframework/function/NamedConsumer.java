package net.openhft.chronicle.testframework.function;

import net.openhft.chronicle.testframework.internal.function.VanillaNamedConsumer;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * The NamedConsumer interface extends both HasName and Consumer interfaces,
 * thus providing a Consumer with an associated name.
 * <p>
 * NamedConsumer can be used in contexts where consumers are managed and identified by their names.
 *
 * @param <T> the type of the input to the consumer
 */
public interface NamedConsumer<T> extends HasName, Consumer<T> {

    /**
     * Creates a NamedConsumer instance from the given consumer and name.
     *
     * @param consumer the Consumer instance
     * @param name     the name to associate with the consumer
     * @return a NamedConsumer wrapping the given consumer and name
     * @throws NullPointerException if either consumer or name is null
     */
    @NotNull
    static <T> NamedConsumer<T> of(@NotNull final Consumer<T> consumer,
                                   @NotNull final String name) {
        requireNonNull(consumer);
        requireNonNull(name);
        return new VanillaNamedConsumer<>(consumer, name);
    }

    /**
     * Creates a NamedConsumer instance from the given ThrowingConsumer and name.
     * ThrowingConsumer is a special type of Consumer that can throw checked exceptions.
     *
     * @param consumer the ThrowingConsumer instance
     * @param name     the name to associate with the consumer
     * @return a NamedConsumer wrapping the given ThrowingConsumer and name
     * @throws NullPointerException if either consumer or name is null
     */
    @NotNull
    static <T> NamedConsumer<T> ofThrowing(@NotNull final ThrowingConsumer<T, ?> consumer,
                                           @NotNull final String name) {
        return of(ThrowingConsumer.of(consumer), name);
    }
}
