//
// Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

package net.openhft.chronicle.testframework.internal.function;

import net.openhft.chronicle.testframework.function.NamedConsumer;

import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * Adapts a {@link Consumer} with a user supplied name.
 *
 * @param <T> type accepted by the consumer
 */
public final class VanillaNamedConsumer<T> implements NamedConsumer<T> {

    private final Consumer<T> consumer;
    private final String name;

    /**
     * Creates an instance that delegates to the given consumer and reports the provided name.
     *
     * @param consumer the Consumer to invoke
     * @param name     name used when reporting
     */
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
