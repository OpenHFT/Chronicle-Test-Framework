package net.openhft.chronicle.testframework.function;

import org.jetbrains.annotations.NotNull;

public interface HasName {

    /**
     * Returns the name of this object.
     *
     * @return name
     */
    @NotNull
    String name();

}