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
 * DtoTester is an interface for testing Data Transfer Objects (DTOs).
 * It provides various methods to set up and execute tests that include
 * accessor checks, validation, mutation, and resetting.
 */
public interface DtoTester {

    /**
     * Executes all the configured tests on the DTO.
     */
    void test();

    /**
     * Builds a new DtoTester with the given type and constructor.
     *
     * @param type        The class of the DTO.
     * @param constructor A supplier that constructs instances of the DTO.
     * @return A builder instance to configure the tester.
     */
    @NotNull
    static <T> Builder<T> builder(@NotNull final Class<T> type,
                                  @NotNull final Supplier<? extends T> constructor) {
        requireNonNull(type);
        return new DtoTesterBuilder<>(type, constructor);
    }

    /**
     * Builder interface for configuring and building a DtoTester instance.
     *
     * @param <T> The type of the DTO.
     */
    interface Builder<T> {

        /**
         * Adds accessors to the builder for testing.
         *
         * @param getter The getter function.
         * @param setter The setter function.
         * @return This builder, for chaining.
         */
        @NotNull <R> Builder<T> withAccessors(@NotNull Function<? super T, ? extends R> getter,
                                              @NotNull BiConsumer<? super T, ? super R> setter);

        /**
         * Adds a resetter function to the builder.
         *
         * @param resetter The reset function.
         * @return This builder, for chaining.
         */
        @NotNull Builder<T> withResetter(@NotNull Consumer<? super T> resetter);

        /**
         * Adds a validation function to the builder.
         *
         * @param validator The validation function.
         * @return This builder, for chaining.
         */
        @NotNull Builder<T> withValidator(@NotNull Consumer<? super T> validator);

        /**
         * Adds a mutator to the builder for testing.
         *
         * @param type        The type of the mutator (MANDATORY/OPTIONAL).
         * @param mutatorName The name of the mutator.
         * @param mutator     The mutation function.
         * @return This builder, for chaining.
         */
        @NotNull <R> Builder<T> addMutator(@NotNull MutatorType type,
                                           @NotNull String mutatorName,
                                           @NotNull Consumer<? super T> mutator);

        /**
         * Adds a mutator to the builder for testing, with a specified value.
         *
         * @param mutatorType The type of the mutator (MANDATORY/OPTIONAL).
         * @param mutatorName The name of the mutator.
         * @param setter      The setter function.
         * @param value       The value to set.
         * @return This builder, for chaining.
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
        MANDATORY, OPTIONAL
    }
}
