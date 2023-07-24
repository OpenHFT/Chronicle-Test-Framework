package net.openhft.chronicle.testframework.exception;

import net.openhft.chronicle.testframework.internal.VanillaExceptionTracker;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A test utility class for recording and executing assertions about the presence (or absence) of exceptions
 *
 * @param <T> The class used to represent thrown exceptions
 */
public interface ExceptionTracker<T> {

    /**
     * Create an exception tracker
     *
     * @param messageExtractor   The function used to extract the String message or description from T
     * @param throwableExtractor The function used to extract the Throwable from T
     * @param resetRunnable      A Runnable that will be called at the end of {@link #checkExceptions()}
     * @param exceptions         A map that will be populated with T as the key and the count of occurrences of T as a value
     * @param ignorePredicate    A predicate that will exclude T's from consideration
     * @param exceptionRenderer  A function to render T as a String (used when dumping exceptions)
     */
    static <T> ExceptionTracker<T> create(@NotNull final Function<T, String> messageExtractor,
                                          @NotNull final Function<T, Throwable> throwableExtractor,
                                          @NotNull final Runnable resetRunnable,
                                          @NotNull final Map<T, Integer> exceptions,
                                          @NotNull final Predicate<T> ignorePredicate,
                                          @NotNull final Function<T, String> exceptionRenderer) {
        return new VanillaExceptionTracker<>(
                messageExtractor,
                throwableExtractor,
                resetRunnable,
                exceptions,
                ignorePredicate,
                exceptionRenderer);
    }

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
     * Determine if the tracker contains an exception matching the predicate
     *
     * @param predicate The predicate to match the exception
     */
    boolean hasException(Predicate<T> predicate);

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
