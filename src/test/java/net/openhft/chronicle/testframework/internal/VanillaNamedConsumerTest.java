package net.openhft.chronicle.testframework.internal;

import net.openhft.chronicle.testframework.internal.function.VanillaNamedConsumer;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class VanillaNamedConsumerTest {

    @Test
    void accept() {
        final AtomicInteger x = new AtomicInteger();
        final VanillaNamedConsumer<AtomicInteger> instance = new VanillaNamedConsumer<>(AtomicInteger::getAndIncrement, "getAndIncrement");
        instance.accept(x);
        assertEquals(1, x.get());
    }

    @Test
    void name() {
        final String name = "foo";
        final VanillaNamedConsumer<Void> instance = new VanillaNamedConsumer<>(v -> {}, name);
        assertEquals(name, instance.name());
    }

    @Test
    void nullInConstructorConsumer() {
        assertThrows(NullPointerException.class, () -> new VanillaNamedConsumer<>(null, ""));
    }

    @Test
    void nullInConstructorName() {
        assertThrows(NullPointerException.class, () -> new VanillaNamedConsumer<>(v -> {}, null));
    }

}