package net.openhft.chronicle.testframework.process;

import net.openhft.chronicle.testframework.internal.process.InternalJavaProcessBuilder;

import java.io.IOException;

import static java.util.Objects.requireNonNull;

/**
 * @deprecated for removal. Use the JavaProcessBuilder instead.
 */
@Deprecated
public final class ProcessRunner {

    private ProcessRunner() {
        // Suppresses default constructor, ensuring non-instantiability.
    }

    /**
     * Spawn a process running the main method of a specified class
     *
     * @param clazz The class to execute
     * @param args  Any arguments to pass to the process
     * @return the Process spawned
     * @throws IOException if there is an error starting the process
     * @deprecated for removal. Use {@link JavaProcessBuilder}
     */
    @Deprecated
    public static Process runClass(Class<?> clazz, String... args) throws IOException {
        requireNonNull(clazz);
        requireNonNull(args);
        return runClass(clazz, new String[]{}, args);
    }

    /**
     * Spawn a process running the main method of a specified class with custom JVM args
     *
     * @param clazz       The class to execute
     * @param jvmArgs     Any JVM arguments to pass to the process
     * @param programArgs Any arguments to pass to the process
     * @return the Process spawned
     * @throws IOException if there is an error starting the process
     */
    public static Process runClass(Class<?> clazz, String[] jvmArgs, String[] programArgs) throws IOException {
        requireNonNull(clazz);
        requireNonNull(jvmArgs);
        requireNonNull(programArgs);
        return new InternalJavaProcessBuilder(clazz)
                .withJvmArguments(jvmArgs)
                .withProgramArguments(programArgs)
                .start();
    }

    /**
     * Spawn a process running the main method of a specified class with custom JVM args and classpath
     *
     * @param clazz       The class to execute
     * @param jvmArgs     Any JVM arguments to pass to the process
     * @param programArgs Any arguments to pass to the process
     * @param classPath   Classpath entries for the process
     * @return the Process spawned
     * @throws IOException if there is an error starting the process
     */
    public static Process runClass(Class<?> clazz, String[] jvmArgs, String[] programArgs, String[] classPath) throws IOException {
        requireNonNull(clazz);
        requireNonNull(jvmArgs);
        requireNonNull(programArgs);
        requireNonNull(classPath);
        return new InternalJavaProcessBuilder(clazz)
                .withJvmArguments(jvmArgs)
                .withProgramArguments(programArgs)
                .withClasspathEntries(classPath)
                .start();
    }

    /**
     * Log stdout and stderr for a process
     * <p>
     * ProcessBuilder.inheritIO() didn't play nicely with Maven failsafe plugin
     * <p>
     * https://maven.apache.org/surefire/maven-failsafe-plugin/faq.html#corruptedstream
     */
    public static void printProcessOutput(String processName, Process process) {
        requireNonNull(processName);
        requireNonNull(process);
        InternalJavaProcessBuilder.printProcessOutput(processName, process);
    }

}