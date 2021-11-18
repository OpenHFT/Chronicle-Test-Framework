/*
 *
 * Copyright (c) 2006-2020, Speedment, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); You may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package net.openhft.chronicle.testframework.internal;

import net.openhft.chronicle.testframework.Product;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ProductDemoTest {

    @Test
    void print() {
        assertEquals(9,
                Product.of(Arrays.asList("A", "B", "C"), Arrays.asList(1, 2, 3))
                        .peek(System.out::println)
                        .count());
    }

    // Exhaustively tests if various empty collections invariants holds
    @TestFactory
    Stream<DynamicTest> demo() {
        // Operations
        final List<Collection<Integer>> collections = Arrays.asList(new LinkedList<>(), new ArrayList<>(), new HashSet<>());
        // Operations
        final Consumer<Collection<Integer>> empty =
                c -> assertTrue(c.isEmpty(), c.getClass() + ".empty() was false");
        final Consumer<Collection<Integer>> size =
                c -> assertEquals(0, c.size(), c.getClass() + ".size() != 0");
        final Consumer<Collection<Integer>> streamCount =
                c -> assertEquals(0, c.stream().count(), c.getClass() + ".stream().count() != 0");
        final List<Consumer<Collection<Integer>>> operations = Arrays.asList(empty, size, streamCount);

        // DynamicTests
        return DynamicTest.stream(Product.of(collections, operations),
                Objects::toString,
                tuple -> tuple.second().accept(tuple.first())
        );
    }


}