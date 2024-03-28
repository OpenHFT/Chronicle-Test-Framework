package net.openhft.chronicle.testframework.exception;

import net.openhft.chronicle.testframework.internal.VanillaExceptionTracker;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * The ExceptionTracker interface provides a set of methods to record, track, and assert exceptions
 * in a testing context. By defining rules to expect or ignore certain exceptions, it helps in
 * systematically verifying the correct exception handling behavior of code under test.
 *
 * @param <T> The class used to represent thrown exceptions
 */
public interface ExceptionTracker<T> {

    /**
     * Factory method to create an instance of the exception tracker. This method encapsulates
     * the construction of a concrete implementation of the ExceptionTracker interface.
     *
     * @param messageExtractor   Function to extract the String message or description from T
     * @param throwableExtractor Function to extract the Throwable from T
     * @param resetRunnable      Runnable that will be called at the end of {@link #checkExceptions()}
     * @param exceptions         Map to populate with T as the key and count of occurrences as value
     * @param ignorePredicate    Predicate to exclude T's from consideration
     * @param exceptionRenderer  Function to render T as a String (used when dumping exceptions)
     * @return An instance of ExceptionTracker
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
     * Specifies that an exception containing the specified string must be thrown during the test.
     * Failing to do so will cause the test to fail.
     *
     * @param message The string to require
     */
    void expectException(String message);

    /**
     * Specifies that an exception matching the specified predicate must be thrown.
     * Failing to match the exception will cause the test to fail.
     *
     * @param predicate   Predicate to match exceptions
     * @param description Description of the exceptions being required
     */
    void expectException(Predicate<T> predicate, String description);

    /**
     * Ignores exceptions containing the specified string. Matching exceptions will not cause
     * the test to fail.
     *
     * @param message The string to ignore
     */
    void ignoreException(String message);

    /**
     * Ignores exceptions matching the specified predicate. Matching exceptions will not cause
     * the test to fail.
     *
     * @param predicate   Predicate to match the exception
     * @param description Description of the exceptions being ignored
     */
    void ignoreException(Predicate<T> predicate, String description);

    /**
     * Determines if the tracker contains an exception matching the predicate.
     *
     * @param predicate Predicate to match the exception
     * @return true if an exception matching the predicate is found; false otherwise
     */
    boolean hasException(Predicate<T> predicate);

    boolean hasException(String message);

    /**
     * Call this method in a teardown (@After) phase of the test to:
     * <ul>
     *     <li>Verify no non-ignored exceptions were thrown</li>
     *     <li>Assert there is an exception matching each of the expected predicates</li>
     * </ul>
     * Implementations should throw an exception and print a summary if the assertion(s) are violated.
     */
    void checkExceptions();
}
