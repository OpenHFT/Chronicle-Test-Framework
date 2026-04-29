/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.testframework.junit4;

import net.openhft.chronicle.testframework.ChronicleTestConfig;
import net.openhft.chronicle.testframework.SystemPropertyScope;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.Objects.requireNonNull;

/**
 * JUnit 4 {@link TestRule} adapter for applying {@link ChronicleTest} configuration.
 * <p>
 * This rule applies any configured system properties for the duration of a single test
 * method invocation.
 */
public final class ChronicleTestRule implements TestRule {

    private static final ReentrantLock SYSTEM_PROPERTIES_LOCK = new ReentrantLock(true);

    private final ChronicleTestConfig baseConfig;

    private ChronicleTestRule(final ChronicleTestConfig baseConfig) {
        this.baseConfig = requireNonNull(baseConfig);
    }

    /**
     * Create a rule with default configuration.
     *
     * @return a new rule instance
     */
    public static ChronicleTestRule create() {
        return new ChronicleTestRule(ChronicleTestConfig.builder().build());
    }

    /**
     * Return a copy of this rule with an additional system property.
     *
     * @param key property key
     * @param value property value
     * @return a new rule instance
     */
    public ChronicleTestRule withSystemProperty(final String key, final String value) {
        ChronicleTestConfig cfg = ChronicleTestConfig.builder()
                .withSystemProperties(baseConfig.systemProperties())
                .withSerialisedSystemProperties(baseConfig.serialiseSystemProperties())
                .withSystemProperty(key, value)
                .build();
        return new ChronicleTestRule(cfg);
    }

    /**
     * Return a copy of this rule with the system property serialisation flag updated.
     *
     * @param serialise true to serialise system property changes
     * @return a new rule instance
     */
    public ChronicleTestRule withSerialisedSystemProperties(final boolean serialise) {
        ChronicleTestConfig cfg = ChronicleTestConfig.builder()
                .withSystemProperties(baseConfig.systemProperties())
                .withSerialisedSystemProperties(serialise)
                .build();
        return new ChronicleTestRule(cfg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Statement apply(final Statement base, final Description description) {
        final ChronicleTestConfig config = mergedConfig(description);
        if (config.systemProperties().isEmpty())
            return base;

        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                boolean locked = false;
                SystemPropertyScope scope = null;
                try {
                    if (config.serialiseSystemProperties()) {
                        SYSTEM_PROPERTIES_LOCK.lock();
                        locked = true;
                    }
                    scope = SystemPropertyScope.apply(config.systemProperties());
                    base.evaluate();
                } finally {
                    if (scope != null)
                        scope.close();
                    if (locked)
                        SYSTEM_PROPERTIES_LOCK.unlock();
                }
            }
        };
    }

    private ChronicleTestConfig mergedConfig(final Description description) {
        final Map<String, String> properties = new LinkedHashMap<>(baseConfig.systemProperties());

        final Class<?> testClass = description.getTestClass();
        final ChronicleTest classAnnotation = testClass == null ? null : testClass.getAnnotation(ChronicleTest.class);
        final ChronicleTest methodAnnotation = description.getAnnotation(ChronicleTest.class);

        if (classAnnotation != null)
            addSystemProperties(properties, classAnnotation.systemProperties());
        if (methodAnnotation != null)
            addSystemProperties(properties, methodAnnotation.systemProperties());

        boolean serialise = baseConfig.serialiseSystemProperties();
        if (classAnnotation != null)
            serialise = classAnnotation.serialiseSystemProperties();
        if (methodAnnotation != null)
            serialise = methodAnnotation.serialiseSystemProperties();

        return ChronicleTestConfig.builder()
                .withSystemProperties(properties)
                .withSerialisedSystemProperties(serialise)
                .build();
    }

    private static void addSystemProperties(final Map<String, String> target,
                                            final ChronicleTest.SystemProperty[] properties) {
        for (ChronicleTest.SystemProperty property : properties) {
            target.put(property.key(), property.value());
        }
    }
}
