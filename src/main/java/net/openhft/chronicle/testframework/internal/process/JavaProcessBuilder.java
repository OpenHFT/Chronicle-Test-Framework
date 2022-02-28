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

public final class JavaProcessBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaProcessBuilder.class);

    private final Class<?> mainClass;
    private String[] jvmArguments;
    private String[] programArguments;
    private String[] classpathEntries;
    private boolean inheritIO = false;

    private JavaProcessBuilder(Class<?> mainClass) {
        this.mainClass = mainClass;
    }

    public static JavaProcessBuilder forMainClass(Class<?> mainClass) {
        return new JavaProcessBuilder(mainClass);
    }

    /**
     * Provide program arguments to execute with
     *
     * @param programArguments The list of program arguments
     * @return this builder
     */
    public JavaProcessBuilder withProgramArguments(String... programArguments) {
        this.programArguments = programArguments;
        return this;
    }

    /**
     * Provide JVM arguments to execute with
     *
     * @param jvmArguments The list of JVM arguments
     * @return this builder
     */
    public JavaProcessBuilder withJvmArguments(String... jvmArguments) {
        this.jvmArguments = jvmArguments;
        return this;
    }

    /**
     * Provide classpath entries to run with, by default uses classpath of spawning process
     *
     * @param classpathEntries The classpath entries to run with
     * @return this builder
     */
    public JavaProcessBuilder withClasspathEntries(String... classpathEntries) {
        this.classpathEntries = classpathEntries;
        return this;
    }

    /**
     * Make the spawned process inherit the IO streams of the spawning process
     * <p>
     * Note: good for testing locally, shouldn't be done in CI because it breaks Maven surefire
     *
     * @return this builder
     */
    public JavaProcessBuilder inheritingIO() {
        this.inheritIO = true;
        return this;
    }

    /**
     * Start the process
     *
     * @return The executing {@link Process}
     */
    public Process start() {
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
        if (classpathEntries == null || classpathEntries.length == 0) {
            classPath = System.getProperty("java.class.path");
        } else {
            classPath = String.join(System.getProperty("path.separator"), classpathEntries);
        }

        String className = mainClass.getName();
        String javaBin = findJavaBinPath().toString();
        List<String> allArgs = new ArrayList<>();
        allArgs.add(javaBin);
        allArgs.addAll(jvmArgsWithoutJavaAgents);
        allArgs.addAll(Arrays.asList(jvmArguments));
        allArgs.add("-cp");
        allArgs.add(classPath);
        allArgs.add(className);
        allArgs.addAll(Arrays.asList(programArguments));
        ProcessBuilder processBuilder = new ProcessBuilder(allArgs.toArray(new String[]{}));
        if (inheritIO) {
            LOGGER.warn("You've specified to inherit IO when spawning a Java process, this won't play nice with Maven surefire plugin, don't commit this, see https://maven.apache.org/surefire/maven-failsafe-plugin/faq.html#corruptedstream");
            processBuilder.inheritIO();
        }
        try {
            return processBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
}
