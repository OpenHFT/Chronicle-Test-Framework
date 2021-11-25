package net.openhft.chronicle.testframework.internal.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class InternalProcessRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternalProcessRunner.class);

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
     */
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
     */
    public static Process runClass(Class<?> clazz, String[] jvmArgs, String[] programArgs, String[] classPathEntries) throws IOException {
        // Because Java17 must be run using various module flags, these must be propagated
        // to the child processes
        // https://stackoverflow.com/questions/1490869/how-to-get-vm-arguments-from-inside-of-java-application
        final RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();

        // filter out javaagent params, or this confuses the IntelliJ debugger
        final List<String> jvmArgsWithoutJavaAgents = runtimeMxBean.getInputArguments().stream()
                .filter(arg -> !arg.startsWith("-javaagent:"))
                .filter(arg -> !arg.startsWith("-agentlib:"))
                .collect(Collectors.toList());

        String classPath;
        if (classPathEntries == null || classPathEntries.length == 0) {
            classPath = System.getProperty("java.class.path");
        } else {
            classPath = String.join(":", classPathEntries);
        }

        String className = clazz.getName();
        String javaBin = findJavaBinPath().toString();
        List<String> allArgs = new ArrayList<>();
        allArgs.add(javaBin);
        allArgs.addAll(jvmArgsWithoutJavaAgents);
        allArgs.addAll(Arrays.asList(jvmArgs));
        allArgs.add("-cp");
        allArgs.add(classPath);
        allArgs.add(className);
        allArgs.addAll(Arrays.asList(programArgs));
        ProcessBuilder processBuilder = new ProcessBuilder(allArgs.toArray(new String[]{}));
//        processBuilder.inheritIO(); // this doesn't place nice with surefire
        return processBuilder.start();
    }

    /**
     * Log stdout and stderr for a process
     * <p>
     * ProcessBuilder.inheritIO() didn't play nicely with Maven failsafe plugin
     * <p>
     * https://maven.apache.org/surefire/maven-failsafe-plugin/faq.html#corruptedstream
     */
    public static void printProcessOutput(String processName, Process process) {
        if (LOGGER.isInfoEnabled())
            LOGGER.info(
                    String.format("%n Output for %s%n stdout:%n%s stderr:%n%s",
                            processName,
                            copyStreamToString(process.getInputStream()),
                            copyStreamToString(process.getErrorStream()))
            );
    }

    /**
     * Copies a stream to a string, up to the point where reading more would block
     *
     * @param inputStream The stream to read from
     * @return The output as a string
     */
    private static String copyStreamToString(InputStream inputStream) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int read;
        try {
            while (inputStream.available() > 0 && (read = inputStream.read(buffer)) >= 0) {
                os.write(buffer, 0, read);
            }
        } catch (IOException e) {
            // Ignore
        }
        return new String(os.toByteArray(), Charset.defaultCharset());
    }

    /**
     * Try and work out what the java executable is cross platform
     *
     * @return the Path to the java executable
     * @throws IllegalStateException if the executable couldn't be located
     */
    private static Path findJavaBinPath() {
        final Path javaBinPath = Paths.get(System.getProperty("java.home")).resolve("bin");
        final Path linuxJavaExecutable = javaBinPath.resolve("java");
        if (linuxJavaExecutable.toFile().exists()) {
            return linuxJavaExecutable;
        } else {
            Path windowsJavaExecutable = javaBinPath.resolve("java.exe");
            if (windowsJavaExecutable.toFile().exists()) {
                return windowsJavaExecutable;
            }
        }
        throw new IllegalStateException("Couldn't locate java executable!");
    }
}
