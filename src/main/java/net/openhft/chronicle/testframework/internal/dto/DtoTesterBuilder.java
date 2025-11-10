//
// Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

package net.openhft.chronicle.testframework.internal.dto;

import net.openhft.chronicle.testframework.dto.DtoTester;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.*;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

/**
 * Builder used by {@link DtoTester} implementations.
 * <p>
 * Mutator functions and validation rules are added here and stored with the
 * names supplied by the caller. Mutators are split into mandatory and optional
 * groups. The mandatory set describes the smallest collection of changes
 * required for the DTO to become valid. Optional mutators are applied
 * individually and in combination so that the tester can verify that each
 * change affects equality and hashCode and that the resetter and validator
 * behave as expected.
 */
public final class DtoTesterBuilder<T> implements DtoTester.Builder<T> {

    @NotNull
    private final Class<T> type;
    @NotNull
    private final Supplier<? extends T> supplier;
    @NotNull
    private final List<NamedMutator<T>> mandatoryMutators;
    @NotNull
    private final List<NamedMutator<T>> optionalMutators;
    @NotNull
    private final List<NamedPredicate<T>> validations;

    private Consumer<? super T> resetter;
    private Consumer<? super T> validator;

    /**
     * Creates a builder for the supplied DTO type.
     *
     * @param type     class of the DTO under test
     * @param supplier supplier used to create new instances
     */
    public DtoTesterBuilder(@NotNull final Class<T> type,
                            @NotNull final Supplier<? extends T> supplier) {
        this.type = requireNonNull(type);
        this.supplier = requireNonNull(supplier);
        mandatoryMutators = newList();
        optionalMutators = newList();
        validations = newList();
    }

    /**
     * Placeholder for future accessor checks.
     */
    @Override
    @NotNull
    public <R> DtoTester.Builder<T> withAccessors(@NotNull final Function<? super T, ? extends R> getter,
                                                  @NotNull final BiConsumer<? super T, ? super R> setter) {
        requireNonNull(getter);
        requireNonNull(setter);
        // Do nothing at the moment.
        return this;
    }

    /**
     * Registers a function that resets all fields of the DTO.
     *
     * @param resetter action that clears the state of a DTO
     * @return this builder for chaining
     */
    @Override
    @NotNull
    public DtoTester.Builder<T> withResetter(@NotNull final Consumer<? super T> resetter) {
        this.resetter = requireNonNull(resetter);
        return this;
    }

    /**
     * Supplies a validator that throws if the DTO is in an invalid state.
     *
     * @param validator validation logic to execute
     * @return this builder for chaining
     */
    @Override
    @NotNull
    public DtoTester.Builder<T> withValidator(@NotNull final Consumer<? super T> validator) {
        this.validator = validator;
        return this;
    }

    /**
     * Adds a mutator to be applied during testing.
     *
     * @param type        whether the mutator is mandatory or optional
     * @param mutatorName descriptive name of the mutator
     * @param mutator     mutation logic
     * @return this builder for chaining
     */
    @Override
    @NotNull
    public <R> DtoTester.Builder<T> addMutator(@NotNull final DtoTester.MutatorType type,
                                               @NotNull final String mutatorName,
                                               @NotNull final Consumer<? super T> mutator) {
        switch (type) {
            case MANDATORY: {
                mandatoryMutators.add(new NamedMutator<>(mutatorName, mutator));
                break;
            }
            case OPTIONAL: {
                optionalMutators.add(new NamedMutator<>(mutatorName, mutator));
                break;
            }
            default:
                throw new IllegalStateException(type.toString());
        }
        return this;
    }
/*

    @Override
    @NotNull
    public DtoTester.Builder<T> addValidationRule(@NotNull final String ruleName,
                                                  @NotNull final Predicate<? super T> validator) {
        validations.add(new NamedPredicate<>(ruleName, validator));
        return this;
    }

    */

    /**
     * Creates a {@link DtoTester} using the information added so far.
     *
     * @return a configured tester instance
     */
    @Override
    public DtoTester build() {
        return new StandardDtoTester<>(this);
    }

    Class<T> type() {
        return type;
    }

    Supplier<? extends T> supplier() {
        return supplier;
    }

    List<NamedMutator<T>> mandatoryMutators() {
        return unmodifiableList(mandatoryMutators);
    }

    List<NamedMutator<T>> optionalMutators() {
        return unmodifiableList(optionalMutators);
    }

    List<NamedMutator<T>> allMutators() {
        final List<NamedMutator<T>> all = newList();
        all.addAll(mandatoryMutators);
        all.addAll(optionalMutators);
        return unmodifiableList(all);
    }

    List<NamedPredicate<T>> validations() {
        return unmodifiableList(validations);
    }

    Consumer<? super T> resetter() {
        return resetter;
    }

    Consumer<? super T> validator() {
        return validator;
    }

    private <E> List<E> newList() {
        return new ArrayList<>();
    }

    /**
     * Simple holder for a mutator and its name.
     */
    static final class NamedMutator<T> extends AbstractNamedHolderRecord<Consumer<? super T>> {
        public NamedMutator(@NotNull final String name,
                            @NotNull final Consumer<? super T> mutator) {
            super(name, mutator);
        }

        Consumer<? super T> mutator() {
            return holder();
        }
    }

    /**
     * Holder for a validation rule and its name.
     */
    static final class NamedPredicate<T> extends AbstractNamedHolderRecord<Predicate<? super T>> {

        public NamedPredicate(@NotNull final String name,
                              @NotNull final Predicate<? super T> predicate) {
            super(name, predicate);
        }

        Predicate<? super T> predicate() {
            return holder();
        }
    }

    /**
     * Base class for associating a name with a value.
     */
    abstract static class AbstractNamedHolderRecord<H> {
        @NotNull
        private final String name;
        @NotNull
        private final H holder;

        /**
         * Creates a record with a name and value.
         *
         * @param name   descriptive name
         * @param holder value to be held
         */
        public AbstractNamedHolderRecord(@NotNull final String name,
                                         @NotNull final H holder) {
            this.name = requireNonNull(name);
            this.holder = requireNonNull(holder);
        }

        String name() {
            return name;
        }

        protected H holder() {
            return holder;
        }

        @Override
        public String toString() {
            return "{" +
                    "name='" + name + '\'' +
                    ", holder=" + holder +
                    '}';
        }
    }
}
