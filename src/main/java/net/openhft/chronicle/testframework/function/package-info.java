/**
 * This package provides a set of functional interfaces and related utility classes to enhance
 * and extend the standard Java functional programming model.
 *
 * <p>The main components include:
 * <ul>
 *     <li>{@link net.openhft.chronicle.testframework.function.TriConsumer} - A three-arity specialization of {@link java.util.function.Consumer}.</li>
 *     <li>{@link net.openhft.chronicle.testframework.function.ThrowingConsumer} - A version of {@link java.util.function.Consumer} that allows checked exceptions.</li>
 *     <li>{@link net.openhft.chronicle.testframework.function.ThrowingConsumerException} - An exception used to wrap checked exceptions in {@code ThrowingConsumer}.</li>
 *     <li>{@link net.openhft.chronicle.testframework.function.NamedConsumer} - An interface extending {@link java.util.function.Consumer} that has a name.</li>
 * </ul>
 *
 * <p>This package aims to provide more flexible and expressive ways to perform operations
 * on data, especially in scenarios where more complex error handling or multi-argument
 * processing is needed.
 */
package net.openhft.chronicle.testframework.function;
