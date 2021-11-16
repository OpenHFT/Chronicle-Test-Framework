package net.openhft.chronicle.testframework.function;

public class ThrowingConsumerException extends RuntimeException {

    public ThrowingConsumerException(Throwable cause) {
        super(cause);
    }
}