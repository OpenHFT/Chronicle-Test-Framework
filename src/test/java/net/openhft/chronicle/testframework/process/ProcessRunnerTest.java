package net.openhft.chronicle.testframework.process;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("deprecation")
class ProcessRunnerTest {
    @Test
    void testSuccessful() throws IOException, InterruptedException {
        Process process = ProcessRunner.runClass(GoodMain.class);

        assertEquals(0, process.waitFor());

        ProcessRunner.printProcessOutput("Good", process);
    }

    @Test
    void testFailing() throws IOException, InterruptedException {
        Process process = ProcessRunner.runClass(BadMain.class);

        assertEquals(1, process.waitFor());

        ProcessRunner.printProcessOutput("Bad", process);
    }
}
