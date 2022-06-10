package net.openhft.chronicle.testframework.internal;

import net.openhft.chronicle.testframework.Delegation;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

final class DelegationBuilderTest {

    private static final int VAL = 42;

    @Test
    void builder() {
        final List<Foo> list = Arrays.asList(new FooImpl(), new FooImpl());
        final ListOfFoo listOfFoo = new DelegationBuilder<>(list).as(ListOfFoo.class).build();
        assertTrue(listOfFoo.stream()
                .allMatch(foo -> foo.val() == VAL));
    }

    @Test
    void defaultToString() {
        final List<Foo> list = Arrays.asList(new FooImpl(), new FooImpl());
        final ListOfFoo listOfFoo = new DelegationBuilder<>(list).as(ListOfFoo.class).build();
        assertEquals(list.toString(), listOfFoo.toString());
    }

    @Test
    void customToString() {
        final List<Foo> list = Arrays.asList(new FooImpl(), new FooImpl());
        final ListOfFoo listOfFoo = new DelegationBuilder<>(list).as(ListOfFoo.class).toStringFunction(d -> Integer.toString(d.size())).build();
        assertEquals(Integer.toString(list.size()), listOfFoo.toString());
    }

    interface Foo {
        int val();
    }

    private static final class FooImpl implements Foo {

        @Override
        public int val() {
            return VAL;
        }

        @Override
        public String toString() {
            return Integer.toString(val());
        }
    }

    interface ListOfFoo extends List<Foo> {
    }

}