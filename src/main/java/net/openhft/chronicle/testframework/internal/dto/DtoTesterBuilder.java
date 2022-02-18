package net.openhft.chronicle.testframework.internal.dto;

import net.openhft.chronicle.testframework.dto.DtoTester;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.*;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

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

    public DtoTesterBuilder(@NotNull final Class<T> type,
                            @NotNull final Supplier<? extends T> supplier) {
        this.type = requireNonNull(type);
        this.supplier = requireNonNull(supplier);
        mandatoryMutators = newList();
        optionalMutators = newList();
        validations = newList();
    }

    @Override
    @NotNull
    public <R> DtoTester.Builder<T> withAccessors(@NotNull final Function<? super T, ? extends R> getter,
                                                  @NotNull final BiConsumer<? super T, ? super R> setter) {
        requireNonNull(getter);
        requireNonNull(setter);
        // Do nothing at the moment.
        return this;
    }

    @Override
    @NotNull
    public DtoTester.Builder<T> withResetter(@NotNull final Consumer<? super T> resetter) {
        this.resetter = requireNonNull(resetter);
        return this;
    }

    @Override
    @NotNull
    public DtoTester.Builder<T> withValidator(@NotNull final Consumer<? super T> validator) {
        this.validator = validator;
        return this;
    }

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

    @Override
    @NotNull
    public DtoTester.Builder<T> addValidationRule(@NotNull final String ruleName,
                                                  @NotNull final Predicate<? super T> validator) {
        validations.add(new NamedPredicate<>(ruleName, validator));
        return this;
    }

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

    static final class NamedMutator<T> extends AbstractNamedHolderRecord<Consumer<? super T>> {
        public NamedMutator(@NotNull final String name,
                            @NotNull final Consumer<? super T> mutator) {
            super(name, mutator);
        }

        Consumer<? super T> mutator() {
            return holder();
        }
    }

    static final class NamedPredicate<T> extends AbstractNamedHolderRecord<Predicate<? super T>> {

        public NamedPredicate(@NotNull final String name,
                              @NotNull final Predicate<? super T> predicate) {
            super(name, predicate);
        }


        Predicate<? super T> predicate() {
            return holder();
        }

    }

    abstract static class AbstractNamedHolderRecord<H> {
        @NotNull
        private final String name;
        @NotNull
        private final H holder;

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
