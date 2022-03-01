package net.openhft.chronicle.testframework.process;

import net.openhft.chronicle.testframework.internal.process.InternalJavaProcessBuilder;
import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

public interface JavaProcessBuilder {

    /**
     * Provide program arguments to execute with
     *
     * @param programArguments The list of program arguments
     * @return this builder
     */
    JavaProcessBuilder withProgramArguments(@NotNull String... programArguments);

    /**
     * Provide JVM arguments to execute with
     *
     * @param jvmArguments The list of JVM arguments
     * @return this builder
     */
    JavaProcessBuilder withJvmArguments(@NotNull String... jvmArguments);

    /**
     * Provide classpath entries to run with, by default uses classpath of spawning process
     *
     * @param classpathEntries The classpath entries to run with
     * @return this builder
     */
    JavaProcessBuilder withClasspathEntries(@NotNull String... classpathEntries);

    /**
     * Make the spawned process inherit the IO streams of the spawning process
     * <p>
     * Note: good for testing locally, shouldn't be done in CI because it breaks Maven Surefire
     *
     * @return this builder
     */
    JavaProcessBuilder inheritingIO();

    /**
     * Start a process defined by the current state of the builder
     *
     * @return the started Process
     */
    Process start();

    /**
     * Creates and returns a new JavaProcessBuilder.
     *
     * @param mainClass to call when starting the process
     * @return a new JavaProcessBuilder
     */
    static JavaProcessBuilder create(@NotNull final Class<?> mainClass) {
        requireNonNull(mainClass);
        return new InternalJavaProcessBuilder(mainClass);
    }

    /**
     * Log stdout and stderr for a process
     * <p>
     * ProcessBuilder.inheritIO() didn't play nicely with Maven failsafe plugin
     * <p>
     * https://maven.apache.org/surefire/maven-failsafe-plugin/faq.html#corruptedstream
     */
    static void printProcessOutput(String processName, Process process) {
        requireNonNull(processName);
        requireNonNull(process);
        InternalJavaProcessBuilder.printProcessOutput(processName, process);
    }

}