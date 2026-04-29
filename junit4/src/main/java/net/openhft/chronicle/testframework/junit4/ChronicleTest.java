/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.testframework.junit4;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Optional configuration annotation for {@link ChronicleTestRule}.
 * <p>
 * Use {@link org.junit.Rule} with {@link ChronicleTestRule#create()} to enable the rule.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface ChronicleTest {

    /**
     * System properties to apply for the duration of the test scope.
     *
     * @return configured system properties
     */
    SystemProperty[] systemProperties() default {};

    /**
     * Whether system property changes should be serialised across tests.
     *
     * @return {@code true} to serialise system property changes
     */
    boolean serialiseSystemProperties() default true;

    /**
     * Key/value system property configuration for {@link ChronicleTest}.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({})
    @interface SystemProperty {

        /**
         * Returns the system property key.
         *
         * @return system property key
         */
        String key();

        /**
         * Returns the system property value.
         *
         * @return system property value
         */
        String value();
    }
}
