package net.openhft.chronicle.testframework;

import net.openhft.chronicle.testframework.internal.SeriesUtil;

import java.util.stream.LongStream;

public final class Series {

    // Suppresses default constructor, ensuring non-instantiability.
    private Series() {
    }

    /**
     * Creates and returns a new LongStream of powers of two 1, 2, ..., 2^63
     *
     * @return the powers of two series
     */
    public static LongStream powersOfTwo() {
        return SeriesUtil.powersOfTwo();
    }

    /**
     * Creates and returns a new LongStream of powers of two and adjacent values
     * 0, 1, 2, ..., 15, 16, 17, 31, 32, 33
     *
     * @return the powers of two and adjacent series
     */
    public static LongStream powersOfTwoAndAdjacent() {
        return SeriesUtil.powersOfTwoAndAdjacent();
    }

    /**
     * Creates and returns a new LongStream of the fibonacci series 0, 1, 1, 2, 3, 5, ...
     *
     * @return the fibonacci series
     */
    public static LongStream fibonacci() {
        return SeriesUtil.fibonacci();
    }


    /**
     * Creates and returns a new LongStream of all the prime numbers 2, 3, 5, 7, ...
     *
     * @return the prime number series
     */
    public static LongStream primes() {
        return SeriesUtil.primes();
    }

}