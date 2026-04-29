/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.testframework;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Applies system properties for a scope and restores the previous values on close.
 * <p>
 * System properties are JVM-global; callers should consider serialising access when
 * running tests in parallel.
 */
public final class SystemPropertyScope implements AutoCloseable {

    private final Map<String, String> previousValues;
    private final Set<String> previouslyAbsent;

    private SystemPropertyScope(@NotNull final Map<String, String> previousValues,
                                @NotNull final Set<String> previouslyAbsent) {
        this.previousValues = previousValues;
        this.previouslyAbsent = previouslyAbsent;
    }

    /**
     * Apply the provided properties and return a scope that will restore the previous values.
     *
     * @param properties properties to apply
     * @return a scope that restores the previous values on close
     */
    @NotNull
    public static SystemPropertyScope apply(@NotNull final Map<String, String> properties) {
        requireNonNull(properties);
        final Map<String, String> previousValues = new LinkedHashMap<>();
        final Set<String> previouslyAbsent = new LinkedHashSet<>();

        for (Map.Entry<String, String> entry : properties.entrySet()) {
            final String key = requireNonNull(entry.getKey());
            final String value = requireNonNull(entry.getValue());

            final String previousValue = System.getProperty(key);
            if (previousValue == null) {
                previouslyAbsent.add(key);
            } else {
                previousValues.put(key, previousValue);
            }
            System.setProperty(key, value);
        }

        return new SystemPropertyScope(previousValues, previouslyAbsent);
    }

    /**
     * Restore previous values for any properties modified by this scope.
     */
    @Override
    public void close() {
        for (String key : previouslyAbsent) {
            System.clearProperty(key);
        }
        for (Map.Entry<String, String> entry : previousValues.entrySet()) {
            System.setProperty(entry.getKey(), entry.getValue());
        }
    }
}

