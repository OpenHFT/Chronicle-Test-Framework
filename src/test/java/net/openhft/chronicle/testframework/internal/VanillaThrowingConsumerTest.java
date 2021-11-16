package net.openhft.chronicle.testframework.internal;

import net.openhft.chronicle.testframework.function.ThrowingConsumerException;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertThrows;

class VanillaThrowingConsumerTest {

    @Test
    void accept() {
        // Guaranteed to fail (Invalid file path)
        final File file = new File("\u0000");
        final VanillaThrowingConsumer<File> instance = new VanillaThrowingConsumer<>(File::getCanonicalPath);
        assertThrows(ThrowingConsumerException.class, () -> instance.accept(file));
    }
}