package net.openhft.chronicle.testframework.internal;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A test utility class for recording and executing assertions about the presence (or absence) of exceptions
 *
 * @param <T> The class used to represent thrown exceptions
 */
public final class VanillaExceptionTracker<T> implements ExceptionTracker<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(VanillaExceptionTracker.class);

    private final Map<Predicate<T>, String> ignoredExceptions = new LinkedHashMap<>();
    private final Map<Predicate<T>, String> expectedExceptions = new LinkedHashMap<>();
    private final Function<T, String> messageExtractor;
    private final Function<T, Throwable> throwableExtractor;
    private final Runnable resetRunnable;
    private final Map<T, Integer> exceptions;
    private final Predicate<T> ignorePredicate;
    private final Function<T, String> exceptionRenderer;
    private boolean finalised = false;

    public VanillaExceptionTracker(@NotNull final Function<T, String> messageExtractor,
                                   @NotNull final Function<T, Throwable> throwableExtractor,
                                   @NotNull final Runnable resetRunnable,
                                   @NotNull final Map<T, Integer> exceptions,
                                   @NotNull final Predicate<T> ignorePredicate) {
        this(messageExtractor, throwableExtractor, resetRunnable, exceptions, ignorePredicate, String::valueOf);
    }

    public VanillaExceptionTracker(@NotNull final Function<T, String> messageExtractor,
                                   @NotNull final Function<T, Throwable> throwableExtractor,
                                   @NotNull final Runnable resetRunnable,
                                   @NotNull final Map<T, Integer> exceptions,
                                   @NotNull final Predicate<T> ignorePredicate,
                                   @NotNull final Function<T, String> exceptionRenderer) {
        this.messageExtractor = messageExtractor;
        this.throwableExtractor = throwableExtractor;
        this.resetRunnable = resetRunnable;
        this.exceptions = exceptions;
        this.ignorePredicate = ignorePredicate;
        this.exceptionRenderer = exceptionRenderer;
    }

    @Override
    public void expectException(@NotNull String message) {
        expectException(k -> contains(messageExtractor.apply(k), message) || throwableContainsTextRecursive(message, throwableExtractor.apply(k)), message);
    }

    @Override
    public void expectException(Predicate<T> predicate, String description) {
        checkFinalised();
        expectedExceptions.put(predicate, description);
    }

    @Override
    public void ignoreException(@NotNull String message) {
        ignoreException(k -> contains(messageExtractor.apply(k), message) || throwableContainsTextRecursive(message, throwableExtractor.apply(k)), message);
    }

    @Override
    public void ignoreException(Predicate<T> predicate, String description) {
        checkFinalised();
        ignoredExceptions.put(predicate, description);
    }

    @Override
    public boolean hasException(Predicate<T> predicate) {
        return exceptions.keySet().stream().anyMatch(predicate);
    }

    @Override
    public void checkExceptions() {
        checkFinalised();
        finalised = true;
        for (Map.Entry<Predicate<T>, String> expectedException : expectedExceptions.entrySet()) {
            if (!exceptions.keySet().removeIf(expectedException.getKey()))
                throw new AssertionError("No error for " + expectedException.getValue());
        }
        for (Map.Entry<Predicate<T>, String> ignoredException : ignoredExceptions.entrySet()) {
            if (exceptions.keySet().removeIf(ignoredException.getKey()))
                LOGGER.debug("Ignored {}", ignoredException.getValue());
        }

        if (hasExceptions()) {
            dumpException();

            final String msg = exceptions.size() + " exceptions were detected: " + exceptions.keySet().stream().map(messageExtractor::apply).collect(Collectors.joining(", "));
            throw new AssertionError(msg);
        }
        resetRunnable.run();
    }

    private boolean hasExceptions() {
        for (T k : exceptions.keySet()) {
            if (!ignorePredicate.test(k))
                return true;
        }

        return false;
    }

    private void dumpException() {
        for (@NotNull Map.Entry<T, Integer> entry : exceptions.entrySet()) {
            final T key = entry.getKey();
            LOGGER.warn(exceptionRenderer.apply(key), throwableExtractor.apply(key));
            final Integer value = entry.getValue();
            if (value > 1)
                LOGGER.warn("Repeated {} times", value);
        }
    }

    private static boolean contains(String text, String message) {
        return text != null && text.contains(message);
    }

    private void checkFinalised() {
        if (finalised) {
            throw new IllegalStateException("VanillaExceptionTracker is single use, you create it, add expectations/ignores, run tests, call check and then dispose of it.");
        }
    }

    /**
     * Does this Throwable or any of its causes contain the specified text?
     *
     * @param text The substring to search for
     * @return true if there are any matches for it, false otherwise
     */
    private static boolean throwableContainsTextRecursive(@NotNull String text, Throwable throwable) {
        return throwableContainsTextRecursive(text, throwable, new HashSet<>());
    }

    private static boolean throwableContainsTextRecursive(@NotNull String text, Throwable throwable, Set<Integer> seenThrowableIDs) {
        if (throwable == null || seenThrowableIDs.contains(System.identityHashCode(throwable))) {
            return false;
        }
        if (throwable.getMessage() != null && throwable.getMessage().contains(text)) {
            return true;
        }
        seenThrowableIDs.add(System.identityHashCode(throwable));
        return throwableContainsTextRecursive(text, throwable.getCause(), seenThrowableIDs);
    }
}
