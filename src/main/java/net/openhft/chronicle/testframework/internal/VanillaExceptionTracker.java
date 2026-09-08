/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.testframework.internal;

import net.openhft.chronicle.testframework.exception.ExceptionTracker;
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
 * Implementation of {@link ExceptionTracker} used in tests to collect and
 * assert exceptions. Each thrown exception is inserted into the supplied map so
 * that it can be analysed once the test completes. Predicates may be registered
 * to mark expected or ignored exceptions. When {@link #checkExceptions()} is
 * called the tracker:
 * <ul>
 *   <li>verifies that every expected predicate matched at least one exception</li>
 *   <li>removes any ignored exceptions from the map</li>
 *   <li>fails if unexpected exceptions remain</li>
 * </ul>
 * After the check the tracker is finalised and must not be reused.
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
        expectException(k -> containsString(k, message), message);
    }

    @Override
    public void expectException(Predicate<T> predicate, String description) {
        checkFinalised();
        expectedExceptions.put(predicate, description);
    }

    @Override
    public void ignoreException(@NotNull String message) {
        ignoreException(k -> containsString(k, message), message);
    }

    @Override
    public void ignoreException(Predicate<T> predicate, String description) {
        checkFinalised();
        ignoredExceptions.put(predicate, description);
    }

    @Override
    public boolean hasException(Predicate<T> predicate) {
        return snapshot().keySet().stream().anyMatch(predicate);
    }

    @Override
    public boolean hasException(String message) {
        return hasException(k -> containsString(k, message));
    }

    @Override
    public void checkExceptions() {
        checkFinalised();
        finalised = true;
        Throwable failure = null;
        try {
            checkExceptions(snapshot());
        } catch (RuntimeException | Error e) {
            failure = e;
            throw e;
        } finally {
            //! VanillaExceptionTrackerTest#missingExpectationStillResetsHandlers and #unexpectedExceptionStillResetsHandlers
            //! prevent a failed test from retaining recording handlers into the next test. Reset on success alone misses both paths.
            try {
                resetRunnable.run();
            } catch (RuntimeException | Error resetFailure) {
                //! #renderingFailureIsPreservedWhenResetAlsoFails preserves the diagnostic failure instead of masking it with cleanup.
                //! #resetFailureAfterSuccessfulCheckIsPropagated is preservation evidence for the existing successful-check behaviour.
                if (failure == null)
                    throw resetFailure;
                if (failure != resetFailure)
                    failure.addSuppressed(resetFailure);
            }
        }
    }

    private void checkExceptions(Map<T, Integer> snapshot) {
        for (Map.Entry<Predicate<T>, String> expectedException : expectedExceptions.entrySet()) {
            if (!removeMatching(snapshot, expectedException.getKey()))
                throw new AssertionError("No error for " + expectedException.getValue());
        }
        for (Map.Entry<Predicate<T>, String> ignoredException : ignoredExceptions.entrySet()) {
            if (removeMatching(snapshot, ignoredException.getKey()))
                LOGGER.debug("Ignored {}", ignoredException.getValue());
        }

        if (hasExceptions(snapshot)) {
            dumpException(snapshot);

            final String msg = snapshot.size() + " exceptions were detected: "
                    + snapshot.keySet().stream().map(messageExtractor::apply).collect(Collectors.joining(", "));
            throw new AssertionError(msg);
        }
    }

    private Map<T, Integer> snapshot() {
        //! VanillaExceptionTrackerTest#concurrentRecordingDuringRenderingUsesOneSnapshot prevents a late recording from invalidating
        //! diagnostic iteration or changing its summary. #snapshotCopiesEntriesWhileHoldingRecordingMonitor checks the copy's lock:
        //! a synchronizedMap does not synchronise its iterators on its own.
        //! #concurrentRecordingDuringPredicateDoesNotMutateTraversal also requires callbacks outside the map lock, so a callback can
        //! wait for a recording worker without deadlock. One detached copy supplies all phases of a check, including the repeat counts.
        synchronized (exceptions) {
            return new LinkedHashMap<>(exceptions);
        }
    }

    private boolean removeMatching(Map<T, Integer> snapshot, Predicate<T> predicate) {
        return snapshot.entrySet().removeIf(entry -> {
            if (!predicate.test(entry.getKey()))
                return false;
            //! #filteringPreservesRepeatsRecordedAfterTheSnapshot exercises a late repeat while filtering. Preserve records
            //! whose counts changed after the snapshot; unconditional removal would erase those later observations from the live map.
            synchronized (exceptions) {
                exceptions.remove(entry.getKey(), entry.getValue());
            }
            return true;
        });
    }

    /**
     * Helper used by {@code expectException} and {@code ignoreException} to
     * decide whether a recorded exception matches a text fragment. The message
     * of the key and the entire cause chain are searched.
     *
     * @param k       The exception key
     * @param message The string to look for
     * @return {@code true} if the text appears in the exception message or any
     * of the throwables in its stack trace
     */
    private boolean containsString(T k, String message) {
        return contains(messageExtractor.apply(k), message) || throwableContainsTextRecursive(message, throwableExtractor.apply(k));
    }

    /**
     * Null safe helper used by {@link #containsString(Object, String)}.
     *
     * @param text    The text to search
     * @param message The fragment to find
     * @return {@code true} if the fragment is present
     */
    private static boolean contains(String text, String message) {
        return text != null && text.contains(message);
    }

    /**
     * Searches the supplied throwable and its causes for the given text.
     *
     * @param text      The substring to search for
     * @param throwable The starting throwable
     * @return {@code true} if a match is found
     */
    private static boolean throwableContainsTextRecursive(@NotNull String text, Throwable throwable) {
        return throwableContainsTextRecursive(text, throwable, new HashSet<>());
    }

    /**
     * Recursive worker that guards against cycles in the cause chain.
     */
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

    /**
     * Determines whether any recorded exceptions remain after applying the
     * ignore predicate.
     */
    private boolean hasExceptions(Map<T, Integer> snapshot) {
        for (T k : snapshot.keySet()) {
            if (!ignorePredicate.test(k))
                return true;
        }

        return false;
    }

    /**
     * Logs all remaining exceptions and their repeat counts. Used when the
     * test is about to fail.
     */
    private void dumpException(Map<T, Integer> snapshot) {
        for (@NotNull Map.Entry<T, Integer> entry : snapshot.entrySet()) {
            final T key = entry.getKey();
            LOGGER.warn(exceptionRenderer.apply(key), throwableExtractor.apply(key));
            final Integer value = entry.getValue();
            if (value > 1)
                LOGGER.warn("Repeated {} times", value);
        }
    }

    /**
     * Prevents the tracker from being used once {@link #checkExceptions()} has
     * been run.
     */
    private void checkFinalised() {
        if (finalised) {
            throw new IllegalStateException("VanillaExceptionTracker is single use, you create it, add expectations/ignores, run tests, call check and then dispose of it.");
        }
    }
}
