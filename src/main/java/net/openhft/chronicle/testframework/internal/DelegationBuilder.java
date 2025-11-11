/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.testframework.internal;

import net.openhft.chronicle.testframework.Delegation;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * Builder that creates a dynamic proxy delegating all calls to a supplied
 * instance. The proxy can expose a specific interface via {@link #as(Class)}
 * and the {@code toString()} behaviour may be customised with
 * {@link #toStringFunction(Function)}.
 *
 * @param <T> type of the proxy view
 * @param <D> type of the delegate
 */
public final class DelegationBuilder<T, D> implements Delegation.Builder<T, D> {

    private final D delegate;
    @SuppressWarnings("unchecked")
    private Class<T> type = (Class<T>) Object.class;
    private Function<? super D, String> toStringFunction = Objects::toString;

    /**
     * Creates a builder that will delegate to the supplied instance.
     *
     * @param delegate object receiving all method invocations
     */
    public DelegationBuilder(@NotNull final D delegate) {
        this.delegate = requireNonNull(delegate);
    }

    /**
     * Changes the type that the proxy will present.
     *
     * @param type class the delegate should be viewed as
     * @param <N>  new proxy view type
     * @return this builder for chaining
     */
    @Override
    public <N extends D> Delegation.Builder<N, D> as(@NotNull final Class<N> type) {
        requireNonNull(type);
        @SuppressWarnings("unchecked") final DelegationBuilder<N, D> newTypeBuilder = (DelegationBuilder<N, D>) this;
        newTypeBuilder.type = type;
        return newTypeBuilder;
    }

    /**
     * Sets the function used to create the proxy's string form.
     *
     * @param toStringFunction function applied when {@code toString()} is called
     * @return this builder for chaining
     */
    @Override
    public Delegation.Builder<T, D> toStringFunction(Function<? super D, String> toStringFunction) {
        this.toStringFunction = requireNonNull(toStringFunction);
        return this;
    }

    /**
     * Finalises the builder and returns the proxy instance.
     *
     * @return a proxy delegating to the supplied object
     */
    @SuppressWarnings("unchecked")
    @Override
    public T build() {
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type}, (proxy1, method, args) -> {
                    if ("toString".equals(method.getName()) && args == null) {
                        return toStringFunction.apply(delegate);
                    }
                    return method.invoke(delegate, args);
                });
    }

}
