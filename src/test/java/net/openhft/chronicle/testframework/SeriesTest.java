package net.openhft.chronicle.testframework;

import org.junit.jupiter.api.Test;

import java.util.function.Supplier;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class SeriesTest {

    @Test
    void powersOfTwo() {
        final long[] expected = new long[Long.SIZE];
        for (int i = 0; i < Long.SIZE; i++) {
            expected[i] = 1L << i;
        }
        test(expected, Series::powersOfTwo);
    }

    @Test
    void powersOfTwoAndAdjacent() {
        final long[] expected = new long[]{0, 1, 2, 3, 4, 5, 7, 8, 9, 15, 16, 17, 31, 32, 33};
        test(expected, Series::powersOfTwoAndAdjacent);
    }

    @Test
    void fibonacci() {
        final long[] expected = new long[]{0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144};
        test(expected, Series::fibonacci);
    }

    @Test
    void primes() {
        final long[] expected = new long[]{
                2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71,
                73, 79, 83, 89, 97, 101, 103, 107, 109, 113, 127, 131, 137, 139, 149, 151, 157, 163, 167, 173};
        test(expected, Series::primes);
    }


    private static void test(final long[] expected,
                             final Supplier<LongStream> supplier) {
        final long[] actual = supplier.get()
                .limit(expected.length)
                .toArray();
        assertArrayEquals(expected, actual);
    }

}