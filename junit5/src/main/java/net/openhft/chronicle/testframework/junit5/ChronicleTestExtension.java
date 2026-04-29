/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.testframework.junit5;

import net.openhft.chronicle.testframework.ChronicleTestConfig;
import net.openhft.chronicle.testframework.SystemPropertyScope;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * JUnit 5 adapter for applying {@link ChronicleTest} configuration.
 */
public final class ChronicleTestExtension implements BeforeEachCallback, AfterEachCallback {

    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(ChronicleTestExtension.class);
    private static final String SYSTEM_PROPERTY_SCOPE_KEY = "ctf.systemPropertyScope";

    private static final ReentrantLock SYSTEM_PROPERTIES_LOCK = new ReentrantLock(true);

    /**
     * Creates a new extension instance.
     */
    public ChronicleTestExtension() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeEach(final ExtensionContext context) {
        final ChronicleTestConfig config = configFor(context);
        if (config == null || config.systemProperties().isEmpty())
            return;
        context.getStore(NAMESPACE).put(SYSTEM_PROPERTY_SCOPE_KEY, LockedSystemPropertyScope.apply(config));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterEach(final ExtensionContext context) {
        LockedSystemPropertyScope scope =
                context.getStore(NAMESPACE).remove(SYSTEM_PROPERTY_SCOPE_KEY, LockedSystemPropertyScope.class);
        if (scope != null) {
            scope.close();
        }
    }

    private static ChronicleTestConfig configFor(final ExtensionContext context) {
        final ChronicleTest classAnnotation = getAnnotation(context.getTestClass());
        final ChronicleTest methodAnnotation = getAnnotation(context.getTestMethod());

        if (classAnnotation == null && methodAnnotation == null)
            return null;

        final Map<String, String> properties = new LinkedHashMap<>();
        if (classAnnotation != null) {
            addSystemProperties(properties, classAnnotation.systemProperties());
        }
        if (methodAnnotation != null) {
            addSystemProperties(properties, methodAnnotation.systemProperties());
        }

        boolean serialise = true;
        if (classAnnotation != null)
            serialise = classAnnotation.serialiseSystemProperties();
        if (methodAnnotation != null)
            serialise = methodAnnotation.serialiseSystemProperties();

        return ChronicleTestConfig.builder()
                .withSystemProperties(properties)
                .withSerialisedSystemProperties(serialise)
                .build();
    }

    private static ChronicleTest getAnnotation(final Optional<? extends java.lang.reflect.AnnotatedElement> element) {
        return element.map(e -> e.getAnnotation(ChronicleTest.class)).orElse(null);
    }

    private static void addSystemProperties(final Map<String, String> target,
                                            final ChronicleTest.SystemProperty[] properties) {
        for (ChronicleTest.SystemProperty property : properties) {
            target.put(property.key(), property.value());
        }
    }

    private static final class LockedSystemPropertyScope implements ExtensionContext.Store.CloseableResource {
        private final AtomicBoolean closed = new AtomicBoolean(false);
        private final boolean locked;
        private final SystemPropertyScope scope;

        private LockedSystemPropertyScope(final boolean locked, final SystemPropertyScope scope) {
            this.locked = locked;
            this.scope = scope;
        }

        static LockedSystemPropertyScope apply(final ChronicleTestConfig config) {
            boolean locked = false;
            try {
                if (config.serialiseSystemProperties()) {
                    SYSTEM_PROPERTIES_LOCK.lock();
                    locked = true;
                }
                final SystemPropertyScope scope = SystemPropertyScope.apply(config.systemProperties());
                return new LockedSystemPropertyScope(locked, scope);
            } catch (RuntimeException e) {
                if (locked)
                    SYSTEM_PROPERTIES_LOCK.unlock();
                throw e;
            }
        }

        @Override
        public void close() {
            if (!closed.compareAndSet(false, true))
                return;
            try {
                scope.close();
            } finally {
                if (locked)
                    SYSTEM_PROPERTIES_LOCK.unlock();
            }
        }
    }
}
