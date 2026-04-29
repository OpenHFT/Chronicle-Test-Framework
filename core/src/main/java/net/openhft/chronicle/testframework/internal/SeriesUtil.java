/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.testframework.internal;

import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * Helper methods for generating number series used in property-based or parameterised tests.
 * Sequences are returned as lazy {@link LongStream} instances so tests can compose them
 * with additional filters or bounds as required.
 */
public final class SeriesUtil {

    private SeriesUtil() {
    }

    /**
     * Returns the sequence 1, 2, 4 and so on up to {@code 1L << 63}.
     */
    public static LongStream powersOfTwo() {
        return LongStream.range(0, Long.SIZE)
                .map(i -> 1L << i);
    }

    /**
     * Each power of two plus the numbers either side of it, without duplicates.
     */
    public static LongStream powersOfTwoAndAdjacent() {
        return powersOfTwo()
                .flatMap(p -> LongStream.rangeClosed(p - 1, p + 1))
                .distinct();
    }

    /**
     * The Fibonacci numbers starting at 0 and 1.
     */
    public static LongStream fibonacci() {
        return LongStream.concat(
                LongStream.of(0),
                Stream.iterate(new int[]{0, 1}, t -> new int[]{t[1], t[0] + t[1]})
                        .mapToLong(f -> f[1])
        );
    }

    /**
     * All prime numbers from 2 upwards.
     */
    public static LongStream primes() {
        return LongStream.iterate(2, i -> i + 1)
                .filter(SeriesUtil::isPrime);
    }

    private static boolean isPrime(long number) {
        return LongStream.rangeClosed(2, (int) (Math.sqrt(number)))
                .allMatch(n -> number % n != 0);
    }
}
