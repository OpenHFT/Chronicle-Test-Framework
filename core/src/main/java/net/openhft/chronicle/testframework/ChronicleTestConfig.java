/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.testframework;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Immutable configuration for CTF test integration adapters (JUnit 4/5).
 * <p>
 * This type is part of the core artefact so JUnit-specific modules can share a
 * single configuration model without leaking {@code org.junit.*} into the core API.
 */
public final class ChronicleTestConfig {

    private final Map<String, String> systemProperties;
    private final boolean serialiseSystemProperties;

    private ChronicleTestConfig(@NotNull final Builder builder) {
        this.systemProperties = Collections.unmodifiableMap(new LinkedHashMap<>(builder.systemProperties));
        this.serialiseSystemProperties = builder.serialiseSystemProperties;
    }

    /**
     * Create a new builder.
     *
     * @return a builder instance
     */
    @NotNull
    public static Builder builder() {
        return new Builder();
    }

    /**
     * System properties that should be applied for the test scope.
     *
     * @return map of property keys to values
     */
    @NotNull
    public Map<String, String> systemProperties() {
        return systemProperties;
    }

    /**
     * Whether system property scoping should be serialised across tests.
     * <p>
     * System properties are JVM-global; when enabled, adapters should hold a
     * global lock for the duration of the test scope to avoid cross-test interference.
     *
     * @return true to serialise system property changes
     */
    public boolean serialiseSystemProperties() {
        return serialiseSystemProperties;
    }

    /**
     * Builder for {@link ChronicleTestConfig}.
     */
    public static final class Builder {
        private final Map<String, String> systemProperties = new LinkedHashMap<>();
        private boolean serialiseSystemProperties = true;

        private Builder() {
        }

        /**
         * Add a system property to apply for the test scope.
         *
         * @param key property key
         * @param value property value
         * @return this builder
         */
        @NotNull
        public Builder withSystemProperty(@NotNull final String key, @NotNull final String value) {
            systemProperties.put(requireNonNull(key), requireNonNull(value));
            return this;
        }

        /**
         * Add multiple system properties to apply for the test scope.
         * <p>
         * When the same key is provided multiple times, the latest value wins.
         *
         * @param properties map of keys to values
         * @return this builder
         */
        @NotNull
        public Builder withSystemProperties(@NotNull final Map<String, String> properties) {
            requireNonNull(properties);
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                withSystemProperty(entry.getKey(), entry.getValue());
            }
            return this;
        }

        /**
         * Control whether adapters should serialise system property changes across tests.
         *
         * @param serialise true to serialise system property changes
         * @return this builder
         */
        @NotNull
        public Builder withSerialisedSystemProperties(final boolean serialise) {
            this.serialiseSystemProperties = serialise;
            return this;
        }

        /**
         * Build an immutable {@link ChronicleTestConfig}.
         *
         * @return built configuration
         */
        @NotNull
        public ChronicleTestConfig build() {
            return new ChronicleTestConfig(this);
        }
    }
}

