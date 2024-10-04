package net.openhft.chronicle.testframework.codestructure;

import net.openhft.chronicle.testframework.codestructure.internal.ExampleInternal;

public class DelegatesToInternal {

    private final ExampleInternal delegate = new ExampleInternal();

}
