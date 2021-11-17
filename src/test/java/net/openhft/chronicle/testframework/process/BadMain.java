package net.openhft.chronicle.testframework.process;

public class BadMain {
    public static void main(String... args) {
        System.err.println("This is bad main class. Exiting with error.");

        throw new IllegalStateException();
    }
}
