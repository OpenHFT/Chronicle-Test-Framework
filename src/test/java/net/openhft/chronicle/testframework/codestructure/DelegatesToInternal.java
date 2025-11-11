/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.testframework.codestructure;

import net.openhft.chronicle.testframework.codestructure.internal.ExampleInternal;

class DelegatesToInternal {

    private final ExampleInternal delegate = new ExampleInternal();

}
