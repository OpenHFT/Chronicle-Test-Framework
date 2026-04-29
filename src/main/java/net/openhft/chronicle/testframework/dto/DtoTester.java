/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.testframework.dto;

import net.openhft.chronicle.testframework.internal.dto.DtoTesterBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Entry point for testing Data Transfer Objects (DTOs).
 * The {@code builder} creates a tester that performs a series of
 * checks around construction, equality, mutators, reset logic and
 * validation. The interface itself is small so that implementations
 * can focus on the behavioural tests.
 */
public interface DtoTester {

    /**
     * Executes the standard suite of checks for a DTO. The implementation
     * created by {@link #builder(Class, Supplier)} verifies that
     * <ul>
     * <li>a new instance is produced each time,</li>
     * <li>two fresh instances compare equal,</li>
     * <li>the resetter clears each applied mutator,</li>
     * <li>{@code hashCode()} changes after a mutator is used, and</li>
     * <li>validation fails until all mandatory mutators are applied and then
     * succeeds even if optional mutators are used.</li>
     * </ul>
     */
    void test();

    /**
     * Starts building a tester for the supplied DTO class type.
     *
     * @param type        class of the DTO
     * @param constructor supplier creating fresh instances
     * @return builder used to configure the tester
     */
    @NotNull
    static <T> Builder<T> builder(@NotNull final Class<T> type,
                                  @NotNull final Supplier<? extends T> constructor) {
        requireNonNull(type);
        return new DtoTesterBuilder<>(type, constructor);
    }

    /**
     * Builder used to supply mutators, reset logic and validation rules.
     *
     * @param <T> the DTO type
     */
    interface Builder<T> {

        /**
         * Registers a getter and setter pair. Intended for future property
         * level tests.
         *
         * @param getter property read function
         * @param setter property write function
         * @return this builder for chaining
         */
        @NotNull <R> Builder<T> withAccessors(@NotNull Function<? super T, ? extends R> getter,
                                              @NotNull BiConsumer<? super T, ? super R> setter);

        /**
         * Supplies a function that restores the DTO to its initial state after
         * each mutator.
         *
         * @param resetter action that clears all fields
         * @return this builder for chaining
         */
        @NotNull Builder<T> withResetter(@NotNull Consumer<? super T> resetter);

        /**
         * Provides validation logic that throws if the DTO is not valid.
         *
         * @param validator validation rule
         * @return this builder for chaining
         */
        @NotNull Builder<T> withValidator(@NotNull Consumer<? super T> validator);

        /**
         * Registers a mutator with a descriptive name and type.
         *
         * @param type        whether the mutator is mandatory or optional
         * @param mutatorName descriptive name used in diagnostics
         * @param mutator     mutation logic
         * @return this builder for chaining
         */
        @NotNull <R> Builder<T> addMutator(@NotNull MutatorType type,
                                           @NotNull String mutatorName,
                                           @NotNull Consumer<? super T> mutator);

        /**
         * Convenience overload to register a mutator that sets a value.
         *
         * @param mutatorType whether the mutator is mandatory or optional
         * @param mutatorName descriptive name of the mutator
         * @param setter      setter to apply
         * @param value       value to pass to the setter
         * @return this builder for chaining
         */
        default @NotNull <R> Builder<T> addMutator(@NotNull final MutatorType mutatorType,
                                                   @NotNull final String mutatorName,
                                                   @NotNull final BiConsumer<? super T, ? super R> setter,
                                                   @Nullable final R value) {
            requireNonNull(mutatorType);
            requireNonNull(mutatorName);
            requireNonNull(setter);
            return addMutator(mutatorType, mutatorName, t -> setter.accept(t, value));
        }

        /**
         * Builds the DtoTester instance.
         *
         * @return The built DtoTester.
         */
        DtoTester build();
    }

    /**
     * Enum for defining mutator types.
     */
    enum MutatorType {
        /** Mutator that must be applied for validation to pass. */
        MANDATORY,
        /** Mutator that may be applied but is not required for validation. */
        OPTIONAL
    }
}
