/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.testframework.junit4;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ChronicleTestRuleTest {

    private static final String KEY = "ctf.junit4.test.key";
    private static String initial;

    @Rule
    public final ChronicleTestRule ctf = ChronicleTestRule.create();

    @BeforeClass
    public static void captureInitial() {
        initial = System.getProperty(KEY);
        System.clearProperty(KEY);
    }

    @AfterClass
    public static void restoreInitial() {
        if (initial == null) {
            System.clearProperty(KEY);
        } else {
            System.setProperty(KEY, initial);
        }
    }

    @Test
    @ChronicleTest(systemProperties = {@ChronicleTest.SystemProperty(key = KEY, value = "value")})
    public void appliesSystemPropertyForTest() {
        assertEquals("property applied: " + KEY, "value", System.getProperty(KEY));
    }

    @Test
    public void restoresSystemPropertyAfterTest() {
        assertNull("property restored: " + KEY, System.getProperty(KEY));
    }
}
