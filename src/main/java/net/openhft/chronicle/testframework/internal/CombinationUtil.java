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
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package net.openhft.chronicle.testframework.internal;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.function.Function.identity;

/**
 * General Combination support. The class eagerly calculates all combinations.
 *
 * @author Per Minborg
 */
public final class CombinationUtil {

    private CombinationUtil() {
    }

    /**
     * Produce a stream containing every combination of the supplied items.
     * The method iterates over all subset sizes and uses recursion to build
     * each set in insertion order.
     */
    @SafeVarargs
    @SuppressWarnings("varargs") // Creating a List from an array is safe
    public static <T> Stream<Set<T>> of(final T... items) {
        return IntStream.rangeClosed(0, items.length)
                .mapToObj(r -> {
                    @SuppressWarnings("unchecked") final T[] data = (T[]) new Object[r];
                    return combinationHelper(items, data, 0, items.length - 1, 0, r);
                }).flatMap(identity());
    }

    /**
     * Convenience wrapper that generates combinations for a collection.
     */
    @SuppressWarnings("unchecked")
    public static <T> Stream<Set<T>> of(final Collection<T> items) {
        return of((T[]) items.toArray());
    }

    /**
     * Convenience wrapper that generates combinations for a stream.
     */
    @SuppressWarnings("unchecked")
    public static <T> Stream<Set<T>> of(final Stream<T> items) {
        return of((T[]) items.toArray());
    }

    /**
     * Recursively fill {@code data} with a subset of size {@code r} chosen from
     * {@code arr} and emit it once complete.
     */
    private static <T> Stream<Set<T>> combinationHelper(
            T[] arr, T[] data,
            int start, int end,
            int index, int r) {

        if (index == r) {
            return Stream.of(asSet(data, r));
        }

        return IntStream.rangeClosed(start, end)
                .filter(i -> end - i + 1 >= r - index)
                .mapToObj(i -> {
                    data[index] = arr[i];
                    return combinationHelper(arr, data, i + 1, end, index + 1, r);
                }).flatMap(identity());
    }

    /**
     * Collect the first {@code newSize} elements of the array into a
     * {@link LinkedHashSet}.
     */
    private static <T> Set<T> asSet(T[] array, int newSize) {
        return Stream.of(array)
                .limit(newSize)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
