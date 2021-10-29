package net.openhft.chronicle.testframework.internal;

import net.openhft.chronicle.testframework.Combination;
import net.openhft.chronicle.testframework.Permutation;
import org.junit.jupiter.api.Test;

public class CombPermDemo {

    @Test
    void demo() {
        Combination.of("A", "B", "C")
                .flatMap(Permutation::of)
                .distinct()
                .forEach(System.out::println);
    }

}
