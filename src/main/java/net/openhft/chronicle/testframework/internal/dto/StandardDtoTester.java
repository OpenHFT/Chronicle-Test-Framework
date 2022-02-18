package net.openhft.chronicle.testframework.internal.dto;

import net.openhft.chronicle.testframework.dto.DtoTester;
import net.openhft.chronicle.testframework.internal.dto.DtoTesterBuilder.NamedMutator;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

final class StandardDtoTester<T> implements DtoTester {

    private final DtoTesterBuilder<T> builder;

    StandardDtoTester(@NotNull final DtoTesterBuilder<T> builder) {
        this.builder = requireNonNull(builder);
    }

    @Override
    public void test() {
        assertInstanceCanBeCreated();
        assertConstructorNonReuse();
        assertEqualsWorksOnFresh();
        assertResettable();
        checkHashCode();
        assertValidationRules();
    }

    private void assertInstanceCanBeCreated() {
        if (createInstance() == null)
            throw new AssertionError("Instance was null");
    }

    private void assertConstructorNonReuse() {
        if (createInstance() == createInstance())
            throw new AssertionError("The constructor must produce new fresh instances");
    }

    private void assertEqualsWorksOnFresh() {
        if (!createInstance().equals(createInstance()))
            throw new AssertionError("Two distinct fresh instances do not equals() each other");
    }

    private void assertResettable() {
        if (builder.resetter() == null)
            // Nothing to assert
            return;
        final Collection<String> failed = check(t -> builder.resetter().accept(t), (fresh, mutated) -> !fresh.equals(mutated));
        if (!failed.isEmpty())
            throw new AssertionError("Resettable: The mutators " + failed + " were applied but the resetter did not reset the mutations");
    }

    private void checkHashCode() {
        check(t -> {
        }, (fresh, mutated) -> fresh.hashCode() == mutated.hashCode())
                .forEach(n -> System.err.println("WARNING: hashCode() for the mutator " + n + " was not changed"));
    }

    private void assertValidationRules() {
        if (builder.validator() == null)
            // Nothing to assert
            return;

        final List<String> applied = newList();
        final T t = createInstance();
        for (NamedMutator<T> namedMutator : builder.mandatoryMutators()) {
            try {
                builder.validator().accept(t);
                throw new AssertionError("There are mandatory mutators but the validator passed without throwing " +
                        "an Exception on using only " + applied + " applied on a fresh instance -> " + t);
            } catch (Exception e) {
                // Happy path
            }
            namedMutator.mutator().accept(t);
            applied.add(namedMutator.name());
        }
        // We should now pass as all mandatory mutators are applied
        try {
            builder.validator().accept(t);
        } catch (Exception e) {
            throw new AssertionError("Validation did not pass despite having applied " + applied + " -> " + t, e);
        }


        for (NamedMutator<T> namedMutator : builder.optionalMutators()) {
            namedMutator.mutator().accept(t);
            // We shall also pass validation using any and all optional mutators
            builder.validator().accept(t);
        }

    }

    private Collection<String> check(final Consumer<? super T> postMutatorAction,
                                     final BiFunction<T, T, Boolean> tester) {

        final T fresh = createInstance();
        final List<String> failed = newList();
        for (NamedMutator<T> namedMutator : builder.allMutators()) {
            final T t = createInstance();
            namedMutator.mutator().accept(t);
            postMutatorAction.accept(t);
            if (tester.apply(fresh, t)) {
                failed.add(namedMutator.name());
            }
        }
        return failed;
    }

    private T createInstance() {
        return builder.supplier().get();
    }

    private <E> List<E> newList() {
        return new ArrayList<>();
    }

}