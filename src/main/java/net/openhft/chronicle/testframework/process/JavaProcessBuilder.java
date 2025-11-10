//
// Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

package net.openhft.chronicle.testframework.process;

import net.openhft.chronicle.testframework.internal.process.InternalJavaProcessBuilder;
import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

/**
 * Builder for launching a separate Java process.
 * <p>
 * Typical usage is:
 * <pre>
 * {@code
 * JavaProcessBuilder.create(MyMain.class)
 *         .withJvmArguments("-Xmx1g")
 *         .withProgramArguments("arg1", "arg2")
 *         .start();
 * }
 * </pre>
 * When {@link #inheritingIO()} is used the spawned process shares the same
 * standard streams. This is convenient for local debugging but tends to break
 * the Maven Surefire and Failsafe plugins, so avoid it in CI jobs.
 */
public interface JavaProcessBuilder {

    /**
     * Provide program arguments for the spawned process.
     *
     * @param programArguments the arguments passed to the main method
     * @return this builder
     */
    JavaProcessBuilder withProgramArguments(@NotNull String... programArguments);

    /**
     * Provide JVM arguments for the spawned process.
     *
     * @param jvmArguments options and system properties for the new JVM
     * @return this builder
     */
    JavaProcessBuilder withJvmArguments(@NotNull String... jvmArguments);

    /**
     * Provide classpath entries. By default the child uses the parent classpath.
     *
     * @param classpathEntries additional classpath entries for the child
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
     * Log stdout and stderr for a process.
     * <p>
     * Use this when {@link #inheritingIO()} is unsuitable, such as on a CI
     * server. The method captures the output and logs it without inheriting the
     * streams. ProcessBuilder.inheritIO() did not play nicely with the Maven
     * failsafe plugin.
     * <p>
     * https://maven.apache.org/surefire/maven-failsafe-plugin/faq.html#corruptedstream
     */
    static void printProcessOutput(String processName, Process process) {
        requireNonNull(processName);
        requireNonNull(process);
        InternalJavaProcessBuilder.printProcessOutput(processName, process);
    }

    /**
     * Get process stderr
     *
     * @param process The process
     */
    static String getProcessStdErr(Process process) {
        requireNonNull(process);
        return InternalJavaProcessBuilder.getProcessStdErr(process);
    }

    /**
     * Get process stdout
     *
     * @param process The process
     */
    static String getProcessStdOut(Process process) {
        requireNonNull(process);
        return InternalJavaProcessBuilder.getProcessStdOut(process);
    }
}
