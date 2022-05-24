package net.openhft.chronicle.testframework.internal;

import net.openhft.chronicle.testframework.Combination;
import net.openhft.chronicle.testframework.Permutation;
import net.openhft.chronicle.testframework.function.NamedConsumer;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ListDemoTest {

    public static final Predicate<Integer> ODD = v -> v % 2 == 1;

    private static final Collection<NamedConsumer<List<Integer>>> OPERATIONS =
            Arrays.asList(
                    NamedConsumer.of(List::clear, "clear()"),
                    NamedConsumer.of(list -> list.add(1), "add(1)"),
                    NamedConsumer.of(list -> list.remove((Integer) 1), "remove(1)"),
                    NamedConsumer.of(list -> list.addAll(Arrays.asList(2, 3, 4, 5)), "addAll(2,3,4,5)"),
                    NamedConsumer.of(list -> list.removeIf(ODD), "removeIf(ODD)")
            );

    private static final Collection<Supplier<List<Integer>>> CONSTRUCTORS =
            Arrays.asList(
                    ArrayList::new,
                    LinkedList::new,
                    CopyOnWriteArrayList::new,
                    Stack::new,
                    Vector::new);


    @TestFactory
    Stream<DynamicTest> validate() {
        return DynamicTest.stream(Combination.of(OPERATIONS)
                        .flatMap(Permutation::of),
                Object::toString,
                operations -> {
                    List<Integer> l0 = new ArrayList<>();
                    List<Integer> l1 = new LinkedList<>();
                    operations.forEach(op -> {
                        op.accept(l0);
                        op.accept(l1);
                    });
                    assertEquals(l0, l1);
                });
    }

    @TestFactory
    Stream<DynamicTest> validateMany() {
        return DynamicTest.stream(Combination.of(OPERATIONS)
                        .flatMap(Permutation::of),
                Object::toString,
                operations -> {

                    // Create a fresh list of List implementations
                    List<List<Integer>> lists = CONSTRUCTORS.stream()
                            .map(Supplier::get)
                            .collect(Collectors.toList());

                    // For each operation, apply the operation on each list
                    operations.forEach(lists::forEach);

                    // Test the lists pairwise
                    Combination.of(lists)

                            // Filter out only combinations with two lists
                            .filter(set -> set.size() == 2)

                            // Convert the Set to a List for easy access below
                            .map(ArrayList::new)

                            // Assert the pair equals
                            .forEach(pair -> assertEquals(pair.get(0), pair.get(1)));
                });
    }

    @Test
    void print() {
        assertEquals(10, Combination.of(CONSTRUCTORS)
                .filter(l -> l.size() == 2)
                .map(this::toTuple)
                .map(t -> t.map(Supplier::get))
                .map(t -> t.map(Object::getClass))
                .map(t -> t.map(Class::getSimpleName))
                .peek(System.out::println)
                .count());
    }


    private static final class Tuple<T> {
        private final T first;
        private final T second;

        public Tuple(T first, T second) {
            this.first = first;
            this.second = second;
        }

        T first() {
            return first;
        }

        T second() {
            return second;
        }

        public <R> Tuple<R> map(Function<? super T, ? extends R> mapper) {
            requireNonNull(mapper);
            return new Tuple<>(mapper.apply(first), mapper.apply(second));
        }

        @Override
        public String toString() {
            return "[" + first +
                    ", " + second +
                    ']';
        }
    }

    private <T> Tuple<T> toTuple(Set<T> set) {
        if (set.size() != 2)
            throw new IllegalArgumentException("Set must contain exactly two elements");
        final Iterator<T> iterator = set.iterator();
        return new Tuple<>(iterator.next(), iterator.next());
    }

}