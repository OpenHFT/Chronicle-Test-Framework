/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
/*
 *
 * Copyright (c) 2006-2020, Speedment, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); You may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package net.openhft.chronicle.testframework.internal;

import java.util.*;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * Utility methods for enumerating permutations. The algorithm maps the
 * permutation number to the factorial number system and selects elements one
 * by one. It was adapted from
 * https://minborgsjavapot.blogspot.com/2015/07/java-8-master-permutations.html
 *
 * Valid inputs are limited so that all factorial calculations fit within a
 * {@code long}. Collections larger than twenty items will therefore cause an
 * {@link IllegalArgumentException}.
 *
 * @author Per Minborg
 */
public final class PermutationUtil {

    // Suppresses default constructor, ensuring non-instantiability.
    private PermutationUtil() {
    }

    /**
     * Calculates {@code n!} for {@code 0 <= n <= 20}.
     *
     * @param n value to factorise, between zero and twenty inclusive
     * @return factorial of {@code n}
     * @throws IllegalArgumentException if {@code n} is outside the valid range
     */
    public static long factorial(final int n) {
        if (n > 20 || n < 0) {
            throw new IllegalArgumentException(n + " is out of range");
        }
        return LongStream.rangeClosed(2, n).reduce(1, (a, b) -> a * b);
    }

    /**
     * Returns the {@code no}-th permutation of the supplied collection.
     * The collection size must be between zero and twenty inclusive.
     *
     * <p>The method uses the factorial number system to decide which
     * element to remove at each step.</p>
     *
     * @param no    ordinal of the permutation, starting at zero and less
     *              than {@code factorial(items.size())}
     * @param items collection of items to permute
     * @param <T>   item type
     * @return chosen permutation as a {@link List}
     * @throws IllegalArgumentException if the ordinal is out of range
     */
    public static <T> List<T> permutation(final long no, final Collection<T> items) {
        Objects.requireNonNull(items);
        final long count = factorial(items.size());
        if (no < 0 || no >= count) {
            throw new IllegalArgumentException("ordinal " + no + " is out of range");
        }
        return permutationHelper(no,
                new LinkedList<>(items),
                new ArrayList<>());
    }

    private static <T> List<T> permutationHelper(final long no, final LinkedList<T> in, final List<T> out) {
        if (in.isEmpty()) {
            return out;
        }
        final long subFactorial = factorial(in.size() - 1);
        out.add(in.remove((int) (no / subFactorial)));
        return permutationHelper((int) (no % subFactorial), in, out);
    }

    /**
     * Convenience overload accepting an array.
     *
     * @param items array of items to permute
     * @param <T>   item type
     * @return stream of permutations
     */
    @SafeVarargs
    @SuppressWarnings("varargs") // Creating a List from an array is safe
    public static <T> Stream<List<T>> of(final T... items) {
        return of(Arrays.asList(items));
    }

    /**
     * Creates a stream of all permutations of the supplied collection.
     * The collection size must be between zero and twenty inclusive.
     *
     * @param items collection to permute
     * @param <T>   item type
     * @return stream of permutations
     */
    public static <T> Stream<List<T>> of(final Collection<T> items) {
        return LongStream.range(0, factorial(items.size()))
                .mapToObj(no -> permutation(no, items));
    }

    /**
     * Convenience overload that accepts a stream of items.
     * The stream is consumed and converted to an array before creating
     * permutations.
     *
     * @param items stream of items to permute
     * @param <T>   item type
     * @return stream of permutations
     */
    @SuppressWarnings("unchecked")
    public static <T> Stream<List<T>> of(final Stream<T> items) {
        return of((T[]) items.toArray());
    }
}
