package net.openhft.chronicle.testframework.internal;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A test utility class for recording and executing assertions about the presence (or absence) of exceptions
 *
 * @deprecated use {@link net.openhft.chronicle.testframework.ExceptionTracker}
 *
 * @param <T> The class used to represent thrown exceptions
 */
@Deprecated(/* To be removed in x.25 */)
public interface ExceptionTracker<T> extends net.openhft.chronicle.testframework.ExceptionTracker<T> {

    /**
     * @deprecated use {@link net.openhft.chronicle.testframework.ExceptionTracker#create(Function, Function, Runnable, Map, Predicate, Function)}
     */
    @Deprecated(/* To be removed in x.25 */)
    static <T> ExceptionTracker<T> create(@NotNull final Function<T, String> messageExtractor,
                                                                              @NotNull final Function<T, Throwable> throwableExtractor,
                                                                              @NotNull final Runnable resetRunnable,
                                                                              @NotNull final Map<T, Integer> exceptions,
                                                                              @NotNull final Predicate<T> ignorePredicate,
                                                                              @NotNull final Function<T, String> exceptionRenderer) {
        return (ExceptionTracker<T>) net.openhft.chronicle.testframework.ExceptionTracker.create(
                messageExtractor, throwableExtractor, resetRunnable, exceptions, ignorePredicate, exceptionRenderer);
    }
}
