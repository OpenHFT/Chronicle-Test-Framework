/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.testframework;

import net.openhft.chronicle.testframework.internal.SeriesUtil;

import java.util.stream.LongStream;

/**
 * Utility for common numeric series.
 * <p>
 * Each method exposes a particular growth pattern:
 * <ul>
 *     <li>{@link #powersOfTwo()} numbers doubling from one up to two to the power of sixty-three.</li>
 *     <li>{@link #powersOfTwoAndAdjacent()} the powers of two with their immediate neighbours.</li>
 *     <li>{@link #fibonacci()} the Fibonacci sequence where each term is the sum of the previous two.</li>
 *     <li>{@link #primes()} an ascending stream of prime numbers.</li>
 * </ul>
 * This class cannot be instantiated.
 */
public final class Series {

    // Suppresses default constructor, ensuring non-instantiability.
    private Series() {
    }

    /**
     * Streams the powers of two from one to two to the power of sixty-three.
     * Each step doubles the previous value.
     *
     * @return the powers of two series as a {@link LongStream}
     */
    public static LongStream powersOfTwo() {
        return SeriesUtil.powersOfTwo(); // Delegating to internal utility
    }

    /**
     * Streams each power of two together with the number before and after it.
     * Values climb quickly as the next power is reached.
     *
     * @return the powers of two and adjacent series as a {@link LongStream}
     */
    public static LongStream powersOfTwoAndAdjacent() {
        return SeriesUtil.powersOfTwoAndAdjacent(); // Delegating to internal utility
    }

    /**
     * Streams the Fibonacci numbers starting at zero and one. Each term
     * is the sum of the previous two so the gaps widen over time.
     *
     * @return the Fibonacci series as a {@link LongStream}
     */
    public static LongStream fibonacci() {
        return SeriesUtil.fibonacci(); // Delegating to internal utility
    }

    /**
     * Streams the prime numbers in ascending order. The distance between
     * primes tends to grow as the values increase.
     *
     * @return the prime number series as a {@link LongStream}
     */
    public static LongStream primes() {
        return SeriesUtil.primes(); // Delegating to internal utility
    }
}
