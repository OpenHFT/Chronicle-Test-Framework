package net.openhft.chronicle.testframework.dto;

import net.openhft.chronicle.testframework.internal.dto.DtoTesterBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public interface DtoTester {

    void test();

    @NotNull
    static <T> Builder<T> builder(@NotNull final Class<T> type,
                                  @NotNull final Supplier<? extends T> constructor) {
        requireNonNull(type);

        return new DtoTesterBuilder<>(type, constructor);
    }

    interface Builder<T> {

        @NotNull <R> Builder<T> withAccessors(@NotNull Function<? super T, ? extends R> getter,
                                              @NotNull BiConsumer<? super T, ? super R> setter);

        @NotNull Builder<T> withResetter(@NotNull Consumer<? super T> resetter);

        @NotNull Builder<T> withValidator(@NotNull Consumer<? super T> validator);

        @NotNull <R> Builder<T> addMutator(@NotNull MutatorType type,
                                           @NotNull String mutatorName,
                                           @NotNull Consumer<? super T> mutator);

        default @NotNull <R> Builder<T> addMutator(@NotNull final MutatorType mutatorType,
                                                   @NotNull final String mutatorName,
                                                   @NotNull final BiConsumer<? super T, ? super R> setter,
                                                   @Nullable final R value) {
            requireNonNull(mutatorType);
            requireNonNull(mutatorName);
            requireNonNull(setter);
            return addMutator(mutatorType, mutatorName, t -> setter.accept(t, value));
        }


        /*
        @NotNull Builder<T> addValidationRule(@NotNull String ruleName,
                                              @NotNull Predicate<? super T> validator);

        default @NotNull <R> Builder<T> addValidationRule(@NotNull final String ruleName,
                                                          @NotNull final Function<? super T, ? extends R> getter,
                                                          @Nullable final R unexpected) {
            requireNonNull(ruleName);
            requireNonNull(getter);
            return addValidationRule(ruleName, t -> !getter.apply(t).equals(unexpected));
        }
         */

        DtoTester build();

    }

    enum MutatorType {
        MANDATORY, OPTIONAL;
    }

}
