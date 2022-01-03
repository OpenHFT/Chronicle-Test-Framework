package net.openhft.chronicle.testframework.internal;

import net.openhft.chronicle.testframework.FlakyTestRunner.RunnableThrows;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class VanillaFlakyTestRunnerTest {

    @Test
    void runZero() {
        RunnableThrows<IllegalStateException> action = new MyAction(0);
        assertThrows(IllegalArgumentException.class, () ->
                new VanillaFlakyTestRunner().run(action, 0)
        );
    }

    @Test
    void run() {
        RunnableThrows<IllegalStateException> action = new MyAction(0);
        new VanillaFlakyTestRunner().run(action, 1);
    }

    @Test
    void runTry2() {
        RunnableThrows<IllegalStateException> action = new MyAction(1);
        new VanillaFlakyTestRunner().run(action, 2);
    }

    @Test
    void runTry3() {
        RunnableThrows<IllegalStateException> action = new MyAction(2);
        assertThrows(IllegalStateException.class, () ->
                new VanillaFlakyTestRunner().run(action, 2)
        );
    }

    private static final class MyAction implements RunnableThrows<IllegalStateException> {

        private int countDown;

        public MyAction(int countDown) {
            this.countDown = countDown;
        }

        @Override
        public void run() throws IllegalStateException {
            if (countDown <= 0)
                return;
            throw new IllegalStateException("" + countDown--);
        }
    }

}