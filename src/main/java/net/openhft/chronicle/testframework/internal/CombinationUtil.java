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

    @SafeVarargs
    @SuppressWarnings("varargs") // Creating a List from an array is safe
    public static <T> Stream<Set<T>> of(final T... items) {
        return IntStream.rangeClosed(0, items.length)
                .mapToObj(r -> {
                    @SuppressWarnings("unchecked") final T[] data = (T[]) new Object[r];
                    return combinationHelper(items, data, 0, items.length - 1, 0, r);
                }).flatMap(identity());
    }

    @SuppressWarnings("unchecked")
    public static <T> Stream<Set<T>> of(final Collection<T> items) {
        return of((T[]) items.toArray());
    }

    @SuppressWarnings("unchecked")
    public static <T> Stream<Set<T>> of(final Stream<T> items) {
        return of((T[]) items.toArray());
    }

    private static <T> Stream<Set<T>> combinationHelper(
            T[] arr, T[] data,
            int start, int end,
            int index, int r) {

        // Current combination is ready to be printed, print it
        if (index == r) {
            return Stream.of(asSet(data, r));
        }

        // replace index with all possible elements. The condition
        // "end-i+1 >= r-index" makes sure that including one element
        // at index will make a combination with remaining elements
        // at remaining positions
        return IntStream.rangeClosed(start, end)
                .filter(i -> end - i + 1 >= r - index)
                .mapToObj(i -> {
                    data[index] = arr[i];
                    return combinationHelper(arr, data, i + 1, end, index + 1, r);
                }).flatMap(identity());
    }

    private static <T> Set<T> asSet(T[] array, int newSize) {
        return Stream.of(array)
                .limit(newSize)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

}
