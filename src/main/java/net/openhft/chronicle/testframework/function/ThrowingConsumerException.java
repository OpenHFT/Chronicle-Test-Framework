package net.openhft.chronicle.testframework.function;

public class ThrowingConsumerException extends RuntimeException {

    static final long serialVersionUID = -8237762538281151227L;

    public ThrowingConsumerException(Throwable cause) {
        super(cause);
    }
}