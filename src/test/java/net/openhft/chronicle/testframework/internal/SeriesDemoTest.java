package net.openhft.chronicle.testframework.internal;

import net.openhft.chronicle.testframework.Series;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SeriesDemoTest {

    @Test
    void demo() {
        System.out.println("Powers of two:");
        print(Series::powersOfTwo);

        System.out.println("Powers of two and adjacent:");
        print(Series::powersOfTwoAndAdjacent);

        System.out.println("Fibonacci (20):");
        print(() -> Series.fibonacci().limit(20));

        System.out.println("Primes (20):");
        print(() -> Series.primes().limit(20));

        assertEquals(64, Series.powersOfTwo().count());
    }

    private static void print(Supplier<LongStream> supplier) {
        System.out.println(Arrays.toString(supplier.get().toArray()));
    }

}
