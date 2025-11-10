//
// Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

/**
 * Provides the default ArchUnit rule suppliers used by
 * {@link net.openhft.chronicle.testframework.internal.codestructure.CodeStructureVerifier}.
 * <p>
 * Each class in this package implements
 * {@link java.util.function.Supplier Supplier}&lt;{@link com.tngtech.archunit.lang.ArchRule}&gt;
 * and defines a structural constraint. Examples include requiring a main method
 * to invoke {@code DtoAlias.init()} and preventing non-internal classes from
 * extending internal ones.
 * <p>
 * {@code CodeStructureVerifier.Builder} installs these suppliers automatically so
 * that the resulting verifier checks the project against all supplied rules when
 * {@link net.openhft.chronicle.testframework.internal.codestructure.CodeStructureVerifier#verify()}
 * is called.
 */
package net.openhft.chronicle.testframework.internal.codestructure.rules;
