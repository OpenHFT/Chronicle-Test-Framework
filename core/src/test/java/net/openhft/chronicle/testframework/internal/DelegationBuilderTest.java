/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
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
        final ListOfFoo listOfFoo = new DelegationBuilder<>(list).as(ListOfFoo.class).build();
        assertTrue(listOfFoo.stream()
                        .allMatch(foo -> foo.val() == VAL),
                "delegation preserves values (expected=" + VAL + ")");
    }

    @Test
    void defaultToString() {
        final List<Foo> list = Arrays.asList(new FooImpl(), new FooImpl());
        final ListOfFoo listOfFoo = new DelegationBuilder<>(list).as(ListOfFoo.class).build();
        assertEquals(list.toString(), listOfFoo.toString(), "default toString delegates to underlying list");
    }

    @Test
    void customToString() {
        final List<Foo> list = Arrays.asList(new FooImpl(), new FooImpl());
        final ListOfFoo listOfFoo = new DelegationBuilder<>(list).as(ListOfFoo.class).toStringFunction(d -> Integer.toString(d.size())).build();
        assertEquals(Integer.toString(list.size()), listOfFoo.toString(), "custom toString function applied");
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

    private interface ListOfFoo extends List<Foo> {
    }
}
