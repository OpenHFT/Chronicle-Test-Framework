/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.testframework.internal;

import net.openhft.chronicle.testframework.Combination;
import net.openhft.chronicle.testframework.Permutation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CombPermDemoTest {

    @Test
    void demo() {
        assertEquals(16,
                Combination.of("A", "B", "C")
                        .flatMap(Permutation::of)
                        .peek(System.out::println)
                        .count());
    }
}
