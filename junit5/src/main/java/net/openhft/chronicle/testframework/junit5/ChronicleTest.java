/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.testframework.junit5;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Opt-in JUnit 5 integration for Chronicle Test Framework.
 * <p>
 * This annotation registers {@link ChronicleTestExtension} and provides a small,
 * stable configuration surface for common test hygiene tasks.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@ExtendWith(ChronicleTestExtension.class)
public @interface ChronicleTest {

    /**
     * System properties to apply for the duration of the test scope.
     *
     * @return configured system properties
     */
    SystemProperty[] systemProperties() default {};

    /**
     * Whether system property changes should be serialised across tests.
     * <p>
     * System properties are JVM-global; when enabled, the extension holds a global lock
     * for the duration of the test scope.
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
