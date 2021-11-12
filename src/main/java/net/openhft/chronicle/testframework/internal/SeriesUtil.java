package net.openhft.chronicle.testframework.internal;

import java.util.stream.LongStream;
import java.util.stream.Stream;

public class SeriesUtil {

    private SeriesUtil() {
    }

    public static LongStream powersOfTwo() {
        return LongStream.range(0, Long.SIZE)
                .map(i -> 1L << i);
    }

    public static LongStream powersOfTwoAndAdjacent() {
        return powersOfTwo()
                .flatMap(p -> LongStream.rangeClosed(p - 1, p + 1))
                .distinct();
    }

    public static LongStream fibonacci() {
        return LongStream.concat(
                LongStream.of(0),
                Stream.iterate(new int[]{0, 1}, t -> new int[]{t[1], t[0] + t[1]})
                        .mapToLong(f -> f[1])
        );
    }


    public static LongStream primes() {
        return LongStream.iterate(2, i -> i + 1)
                .filter(SeriesUtil::isPrime);
    }

    private static boolean isPrime(long number) {
        return LongStream.rangeClosed(2, (int) (Math.sqrt(number)))
                .allMatch(n -> number % n != 0);
    }

}