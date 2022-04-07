package net.openhft.chronicle.testframework.internal;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A test utility class for recording and executing assertions about the presence (or absence) of exceptions
 *
 * @param <T> The class used to represent thrown exceptions
 */
public final class StandardExceptionTracker<T> implements ExceptionTracker<T> {

    private final Map<Predicate<T>, String> ignoredExceptions = new LinkedHashMap<>();
    private final Map<Predicate<T>, String> expectedExceptions = new LinkedHashMap<>();
    private final Function<T, String> messageExtractor;
    private final Function<T, Throwable> throwableExtractor;
    private final Runnable resetRunnable;
    private final Map<T, Integer> exceptions;

    public StandardExceptionTracker(@NotNull final Function<T, String> messageExtractor,
                                    @NotNull final Function<T, Throwable> throwableExtractor,
                                    @NotNull final Runnable resetRunnable,
                                    @NotNull final Map<T, Integer> exceptions) {
        this.messageExtractor = messageExtractor;
        this.throwableExtractor = throwableExtractor;
        this.resetRunnable = resetRunnable;
        this.exceptions = exceptions;
    }

    @Override
    public void expectException(String message) {
        expectException(k -> contains(messageExtractor.apply(k), message) || (throwableExtractor.apply(k) != null && contains(throwableExtractor.apply(k).getMessage(), message)), message);
    }

    @Override
    public void expectException(Predicate<T> predicate, String description) {
        expectedExceptions.put(predicate, description);
    }

    @Override
    public void ignoreException(String message) {
        ignoreException(k -> contains(messageExtractor.apply(k), message) || (throwableExtractor.apply(k) != null && contains(throwableExtractor.apply(k).getMessage(), message)), message);
    }

    @Override
    public void ignoreException(Predicate<T> predicate, String description) {
        ignoredExceptions.put(predicate, description);
    }

    @Override
    public void checkExceptions() {
        for (Map.Entry<Predicate<T>, String> expectedException : expectedExceptions.entrySet()) {
            if (!exceptions.keySet().removeIf(expectedException.getKey()))
                throw new AssertionError("No error for " + expectedException.getValue());
        }
        expectedExceptions.clear();
        for (Map.Entry<Predicate<T>, String> ignoredException : ignoredExceptions.entrySet()) {
            if (!exceptions.keySet().removeIf(ignoredException.getKey()))
                //Slf4jExceptionHandler.DEBUG.on(getClass(), "Ignored " + ignoredException.getValue());
                System.err.println(getClass() + " DEBUG Ignored " + ignoredException.getValue());
        }
        ignoredExceptions.clear();
        /*
        if (Jvm.hasException(exceptions)) {
            Jvm.dumpException(exceptions);
            TODO: Extract code from JVM an put it here...

*/
            final String msg = exceptions.size() + " exceptions were detected: " + exceptions.keySet().stream().map(messageExtractor::apply).collect(Collectors.joining(", "));
            resetRunnable.run();
            throw new AssertionError(msg);
 /*       }*/
    }

    private static boolean contains(String text, String message) {
        return text != null && text.contains(message);
    }
}
