package net.openhft.chronicle.testframework.internal;

import java.util.function.Predicate;

/**
 * A test utility class for recording and executing assertions about the presence (or absence) of exceptions
 *
 * @param <T> The class used to represent thrown exceptions
 */
public interface ExceptionTracker<T> {

    /**
     * Require than an exception containing the specified string is thrown during the test
     *
     * @param message The string to require
     */
    void expectException(String message);

    /**
     * Require that an exception matching the specified predicate is thrown
     *
     * @param predicate   The predicate used to match exceptions
     * @param description The description of the exceptions being required
     */
    void expectException(Predicate<T> predicate, String description);

    /**
     * Ignore exceptions containing the specified string
     *
     * @param message The string to ignore
     */
    void ignoreException(String message);

    /**
     * Ignore exceptions matching the specified predicate
     *
     * @param predicate   The predicate to match the exception
     * @param description The description of the exceptions being ignored
     */
    void ignoreException(Predicate<T> predicate, String description);

    /**
     * Call this in @After to ensure
     * <ul>
     *     <li>No non-ignored exceptions were thrown</li>
     *     <li>There is an exception matching each of the expected predicates</li>
     * </ul>
     * <p>
     * Implementations should throw an exception and print a summary of the assertion(s) violated
     */
    void checkExceptions();
}
