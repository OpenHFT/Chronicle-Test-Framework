package net.openhft.chronicle.testframework.function;

/**
 * ThrowingConsumerException is a specialized {@link RuntimeException} used to wrap exceptions
 * thrown by a {@link ThrowingConsumer}.
 * <p>
 * This exception allows checked exceptions to be propagated through the ThrowingConsumer interface,
 * and can be used to handle or log the underlying exceptions as needed.
 */
public class ThrowingConsumerException extends RuntimeException {

    /**
     * The serial version UID for the serialization mechanism.
     */
    static final long serialVersionUID = -8237762538281151227L;

    /**
     * Constructs a new ThrowingConsumerException with the specified cause.
     * <p>
     * The cause is used to retain necessary information about the underlying exception that
     * triggered this ThrowingConsumerException, making it available for further inspection or logging.
     *
     * @param cause the underlying cause (usually a checked exception thrown by the ThrowingConsumer)
     */
    public ThrowingConsumerException(Throwable cause) {
        super(cause);
    }
}
