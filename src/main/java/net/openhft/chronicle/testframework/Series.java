package net.openhft.chronicle.testframework;

import net.openhft.chronicle.testframework.internal.SeriesUtil;

import java.util.stream.LongStream;

/**
 * This class provides utility methods to generate series of numbers,
 * such as powers of two, powers of two and adjacent values, Fibonacci series, and prime numbers.
 * <p>
 * This class is a final utility class and cannot be instantiated.
 */
public final class Series {

    // Suppresses default constructor, ensuring non-instantiability.
    private Series() {
    }

    /**
     * Creates and returns a new LongStream of powers of two: 1, 2, ..., 2^63.
     *
     * @return the powers of two series as a LongStream
     */
    public static LongStream powersOfTwo() {
        return SeriesUtil.powersOfTwo(); // Delegating to internal utility
    }

    /**
     * Creates and returns a new LongStream of powers of two and adjacent values:
     * 0, 1, 2, ..., 15, 16, 17, 31, 32, 33.
     *
     * @return the powers of two and adjacent series as a LongStream
     */
    public static LongStream powersOfTwoAndAdjacent() {
        return SeriesUtil.powersOfTwoAndAdjacent(); // Delegating to internal utility
    }

    /**
     * Creates and returns a new LongStream of the Fibonacci series: 0, 1, 1, 2, 3, 5, ...
     *
     * @return the Fibonacci series as a LongStream
     */
    public static LongStream fibonacci() {
        return SeriesUtil.fibonacci(); // Delegating to internal utility
    }


    /**
     * Creates and returns a new LongStream of all the prime numbers: 2, 3, 5, 7, ...
     *
     * @return the prime number series as a LongStream
     */
    public static LongStream primes() {
        return SeriesUtil.primes(); // Delegating to internal utility
    }

}