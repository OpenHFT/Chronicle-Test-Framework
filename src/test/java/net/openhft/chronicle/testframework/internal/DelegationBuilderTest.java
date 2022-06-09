package net.openhft.chronicle.testframework.internal;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

final class DelegationBuilderTest {

    private static final int VAL = 42;

    @Test
    void builder() {
        final List<Foo> list = Arrays.asList(new FooImpl(), new FooImpl());
        final ListOfFoo listOfFoo = new DelegationBuilder<>(ListOfFoo.class, list).build();
        assertTrue(listOfFoo.stream()
                .allMatch(foo -> foo.val() == VAL));
    }


    interface Foo {
        int val();
    }

    private static final class FooImpl implements Foo {

        @Override
        public int val() {
            return VAL;
        }
    }

    interface ListOfFoo extends List<Foo> {
    }

}