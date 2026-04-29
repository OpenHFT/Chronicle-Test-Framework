/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.testframework;

import net.openhft.chronicle.testframework.internal.DelegationBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * Helper for creating dynamic proxies that forward every invocation to a
 * supplied delegate. The fluent API lets callers choose the interface the
 * proxy exposes and customise the result of {@code toString()}. The proxy is
 * constructed with {@link java.lang.reflect.Proxy}.
 * <p>
 * This class cannot be instantiated.
 */
public final class Delegation {

    // Suppresses default constructor, ensuring non-instantiability.
    private Delegation() {
    }

    /**
     * Starts a builder that delegates calls to the supplied {@code delegate}.
     * If {@link Builder#as(Class)} is not invoked the proxy exposes only the
     * methods defined on {@link Object}.
     *
     * @param delegate object receiving all method invocations
     * @param <D>      type of the delegate
     * @return new delegator builder
     * @throws NullPointerException if {@code delegate} is {@code null}
     */
    public static <D> Builder<Object, D> of(@NotNull final D delegate) {
        requireNonNull(delegate);
        return new DelegationBuilder<>(delegate);
    }

    /**
     * Interface for building a delegation object. Allows customisation of the
     * type view and the {@code toString()} method of the delegate. The instance
     * produced by {@link #build()} is a dynamic proxy.
     *
     * @param <T> Target type
     * @param <D> Delegation type
     */
    public interface Builder<T, D> {

        // Future: Add capability to override any method using T::method references

        /**
         * Sets the interface that the proxy will implement. Without calling
         * this method the proxy only exposes the methods of {@link Object}.
         *
         * @param type interface class, non-null
         * @param <N>  new view type
         * @return this builder
         */
        <N extends D> Builder<N, D> as(Class<N> type);

        /**
         * Supplies a function used to produce the proxy's string form. The
         * function is applied to the delegate whenever {@code toString()} is
         * called. If unspecified, the delegate's {@link Object#toString()} is
         * used.
         *
         * @param toStringFunction function applied to the delegate, non-null
         * @return this builder
         */
        Builder<T, D> toStringFunction(Function<? super D, String> toStringFunction);

        /**
         * Builds and returns the configured proxy.
         *
         * @return a dynamic proxy that forwards calls to the delegate
         */
        T build();
    }
}
