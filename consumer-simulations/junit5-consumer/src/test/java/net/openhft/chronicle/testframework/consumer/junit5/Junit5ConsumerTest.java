/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.testframework.consumer.junit5;

import net.openhft.chronicle.testframework.junit5.ChronicleTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class Junit5ConsumerTest {

    private static final String KEY = "ctf.consumer.junit5.key";
    private static String initial;

    @BeforeAll
    static void captureInitial() {
        initial = System.getProperty(KEY);
        System.clearProperty(KEY);
    }

    @AfterAll
    static void restoreInitial() {
        if (initial == null) {
            System.clearProperty(KEY);
        } else {
            System.setProperty(KEY, initial);
        }
    }

    @Test
    @ChronicleTest(systemProperties = {@ChronicleTest.SystemProperty(key = KEY, value = "value")})
    void appliesSystemPropertyForTest() {
        assertEquals("value", System.getProperty(KEY), "property applied: " + KEY);
    }

    @Test
    void restoresSystemPropertyAfterTest() {
        assertNull(System.getProperty(KEY), "property restored: " + KEY);
    }
}
