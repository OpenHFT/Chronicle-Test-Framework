package net.openhft.chronicle.testframework.process;

import org.junit.jupiter.api.Test;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaProcessBuilderTest {

    @Test
    void runnerWorksWithNoArguments() throws InterruptedException {
        final Process start = JavaProcessBuilder.create(NoExpectations.class).start();
        start.waitFor(5, SECONDS);
        assertEquals(0, start.exitValue());
    }

    @Test
    void runnerWorksWithJvmArguments() throws InterruptedException {
        final Process start = JavaProcessBuilder.create(ExpectJvmArgs.class)
                .withJvmArguments("-Dtesting1=123", "-Dtesting2=456")
                .start();
        start.waitFor(5, SECONDS);
        assertEquals(0, start.exitValue());
    }

    @Test
    void runnerWorksWithProgramArguments() throws InterruptedException {
        final Process start = JavaProcessBuilder.create(ExpectProgramArgs.class)
                .withProgramArguments("123", "456")
                .start();
        start.waitFor(5, SECONDS);
        assertEquals(0, start.exitValue());
    }

    @Test
    void runnerWorksWithCustomClasspath() throws InterruptedException {
        final Process start = JavaProcessBuilder.create(NoExpectations.class)
                .withClasspathEntries("nonexistent.jar")
                .start();
        start.waitFor(5, SECONDS);
        // This will fail with class not found
        assertEquals(1, start.exitValue());
        final String processStdErr = JavaProcessBuilder.getProcessStdErr(start);
        assertTrue(processStdErr.contains("net.openhft.chronicle.testframework.process.JavaProcessBuilderTest$NoExpectations"));
    }

    private static final class NoExpectations {
        public static void main(String[] args) {
            System.out.println("Hello world");
        }
    }

    private static final class ExpectProgramArgs {
        public static void main(String[] args) {
            if (!("123".equals(args[0]) && "456".equals(args[1]))) {
                throw new RuntimeException("Expected argument not present");
            }
        }
    }

    private static final class ExpectJvmArgs {
        public static void main(String[] args) {
            if (!("123".equals(System.getProperty("testing1"))
                    && "456".equals(System.getProperty("testing2")))) {
                throw new RuntimeException("Expected system property not present");
            }
        }
    }
}