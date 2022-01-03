package net.openhft.chronicle.testframework;

import net.openhft.chronicle.testframework.internal.VanillaFlakyTestRunner;
import org.jetbrains.annotations.NotNull;

public final class FlakyTestRunner {

    // Suppresses default constructor, ensuring non-instantiability.
    private FlakyTestRunner() {
    }

    /**
     * Runs the provided {@code action} trying at most 2 times.
     *
     * @param action non-null action to perform
     * @param <X>    exception type
     * @throws X if an underlying exception is thrown despite retrying the specified number of times
     */
    public static <X extends Throwable> void run(@NotNull final RunnableThrows<X> action) throws X {
        new VanillaFlakyTestRunner().run(action, 2);
    }

    /**
     * Runs the provided {@code action} trying at most 2 times if the provided {@code flakyOnTHisArch} is true,
     * otherwise just runs the provided {@code action} once.
     *
     * @param flakyOnThisArch indicating if the provided action is
     * @param action          non-null action to perform
     * @param <X>             exception type
     * @throws X if an underlying exception is thrown despite retrying the specified number of times
     */
    public static <X extends Throwable> void run(final boolean flakyOnThisArch,
                                                 @NotNull final RunnableThrows<X> action) throws X {
        new VanillaFlakyTestRunner().run(action, flakyOnThisArch ? 2 : 1);
    }

    /**
     * Runs the provided {@code action} trying at most the provided {@code maxIteration} times if the
     * provided {@code flakyOnTHisArch} is true, otherwise just runs the provided {@code action} once.
     *
     * @param flakyOnThisArch indicating if the provided action is
     * @param action          non-null action to perform
     * @param <X>             exception type
     * @throws X if an underlying exception is thrown despite retrying the specified number of times
     */
    public static <X extends Throwable> void run(final boolean flakyOnThisArch,
                                                 @NotNull final RunnableThrows<X> action,
                                                 final int maxIterations) throws X {
        new VanillaFlakyTestRunner().run(action, flakyOnThisArch ? maxIterations : 1);
    }

    @FunctionalInterface
    public interface RunnableThrows<T extends Throwable> {

        /**
         * Performs an action.
         *
         * @throws T if an underlying exception is thrown
         */
        void run() throws T;
    }

}