package net.openhft.chronicle.testframework.internal.process;

import java.io.IOException;

/**
 * @deprecated Use {@link JavaProcessBuilder} instead
 */
@Deprecated
public final class InternalProcessRunner {

    private InternalProcessRunner() {
        // Suppresses default constructor, ensuring non-instantiability.
    }

    /**
     * Spawn a process running the main method of a specified class
     *
     * @param clazz The class to execute
     * @param args  Any arguments to pass to the process
     * @return the Process spawned
     * @throws IOException if there is an error starting the process
     * @deprecated Use {@link JavaProcessBuilder} instead
     */
    @Deprecated
    public static Process runClass(Class<?> clazz, String... args) throws IOException {
        return runClass(clazz, new String[]{}, args, null);
    }

    /**
     * Spawn a process running the main method of a specified class
     *
     * @param clazz            The class to execute
     * @param jvmArgs          Any JVM arguments to pass to the process
     * @param programArgs      Any arguments to pass to the process
     * @param classPathEntries Classpath for the process, {code null} for default
     * @return the Process spawned
     * @throws IOException if there is an error starting the process
     * @deprecated Use {@link JavaProcessBuilder} instead
     */
    @Deprecated
    public static Process runClass(Class<?> clazz, String[] jvmArgs, String[] programArgs, String[] classPathEntries) throws IOException {
        return JavaProcessBuilder.forMainClass(clazz)
                .withJvmArguments(jvmArgs)
                .withProgramArguments(programArgs)
                .withProgramArguments(programArgs)
                .withClasspathEntries(classPathEntries)
                .start();
    }

    /**
     * Log stdout and stderr for a process
     * <p>
     * ProcessBuilder.inheritIO() didn't play nicely with Maven failsafe plugin
     * <p>
     * https://maven.apache.org/surefire/maven-failsafe-plugin/faq.html#corruptedstream
     *
     * @deprecated Use {@link JavaProcessBuilder#printProcessOutput(String, Process)} instead
     */
    @Deprecated
    public static void printProcessOutput(String processName, Process process) {
        JavaProcessBuilder.printProcessOutput(processName, process);
    }
}
