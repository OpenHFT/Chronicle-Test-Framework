/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
/**
 * Contains components that set up and run ArchUnit based code structure tests.
 *
 * <p>Classes in this package supply rules used by
 * {@link net.openhft.chronicle.testframework.internal.codestructure.CodeStructureVerifier}
 * to assert that the code base follows the expected design. The checks include
 * verifying bootstrap calls and preventing external classes from extending
 * internal types.
 *
 * <p>The contents of this package are for internal use only and may change
 * without notice.
 */
package net.openhft.chronicle.testframework.internal.codestructure;
