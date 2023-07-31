package net.openhft.chronicle.testframework.function;

import org.jetbrains.annotations.NotNull;

/**
 * This interface represents any object that has a name associated with it.
 * Implementing this interface allows a class to have a method that retrieves
 * a name string, providing a consistent way to manage named objects.
 */
public interface HasName {

    /**
     * Returns the name of this object.
     * <p>
     * The name is an identifier that may be used to distinguish this object
     * from other objects in a collection or system. It is not meant to be unique
     * necessarily, but should provide meaningful information to the developers or
     * users.
     *
     * @return name - the name associated with this object, must not be null.
     */
    @NotNull
    String name();
}
