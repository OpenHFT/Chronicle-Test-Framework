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

import net.openhft.chronicle.testframework.Combination;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Per Minborg
 */
final class CombinationTest {

    @Test
    void testOfEmpty() {
        final List<Set<String>> actual = Combination.<String>of()
                .collect(toList());

        final List<Set<String>> expected = singletonList(emptySet());

        assertEquals(expected, actual);
    }

    @Test
    void testOfSingleton() {
        final List<Set<String>> actual = Combination.of("a")
                .collect(toList());

        assertEquals(
                asList(
                        emptySet(),
                        singleton("a")
                ),
                actual
        );
    }

    @Test
    void testOfTwo() {
        final List<Set<String>> actual = Combination.of("a", "b")
                .collect(toList());

        final List<Set<String>> expected = asList(
                emptySet(),
                singleton("a"),
                singleton("b"),
                new LinkedHashSet<>(asList("a", "b"))
        );

        assertEquals(expected, actual);
    }

    @Test
    void testOfThree() {
        final List<Set<String>> actual = Combination.of("a", "b", "c")
                .collect(toList());

        final List<Set<String>> expected = asList(
                emptySet(),
                singleton("a"),
                singleton("b"),
                singleton("c"),
                new LinkedHashSet<>(asList("a", "b")),
                new LinkedHashSet<>(asList("a", "c")),
                new LinkedHashSet<>(asList("b", "c")),
                new LinkedHashSet<>(asList("a", "b", "c"))
        );

        assertEquals(expected, actual);
    }

    @Test
    void testOfTwoList() {
        final List<Set<String>> actual = Combination.of(Arrays.asList("a", "b"))
                .collect(toList());

        final List<Set<String>> expected = asList(
                emptySet(),
                singleton("a"),
                singleton("b"),
                new LinkedHashSet<>(asList("a", "b"))
        );

        assertEquals(expected, actual);
    }

    @Test
    void testOfStream() {
        final List<Set<String>> actual = Combination.of(Stream.of("a", "b"))
                .collect(toList());

        final List<Set<String>> expected = asList(
                emptySet(),
                singleton("a"),
                singleton("b"),
                new LinkedHashSet<>(asList("a", "b"))
        );

        assertEquals(expected, actual);
    }

}
