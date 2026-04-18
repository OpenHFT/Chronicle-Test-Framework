/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.testframework.internal.codestructure.rules;

/**
 * Utility class for sharing rule logic.
 * <p>
 * The {@code INTERNAL_PACKAGE_REGEX} constant matches package names that
 * contain {@code impl} or {@code internal}. It is currently used by
 * {@link NonInternalClassesMustNotExtendInternalClassesRuleSupplier} to
 * ensure non-internal classes do not extend implementation classes. Other
 * rules may reuse this expression when they need to filter out internal
 * packages.
 * </p>
 */
public enum RuleUtil {
    ;
    public static final String INTERNAL_PACKAGE_REGEX = ".*\\.(impl|internal)\\..*";
}
