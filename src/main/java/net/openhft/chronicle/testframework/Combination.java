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
package net.openhft.chronicle.testframework;

import net.openhft.chronicle.testframework.internal.CombinationUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Utility class that provides functionality to generate all possible combinations
 * of a given set of elements. The class eagerly calculates the combinations.
 * <p>
 * This class cannot be instantiated.
 *
 * @author Per Minborg
 */
public final class Combination {

    // Suppresses default constructor, ensuring non-instantiability.
    private Combination() {
    }

    /**
     * Creates and returns a stream of all possible combinations of the given elements.
     * <p>
     * The order of the combinations in the stream is unspecified.
     *
     * @param <T>   element type
     * @param items the array of items to combine
     * @return a stream of sets containing all possible combinations of the given elements
     * @throws NullPointerException if the provided {@code items} array is {@code null}.
     */
    @SafeVarargs
    @SuppressWarnings("varargs") // Creating a Set from an array is safe
    public static <T> Stream<Set<T>> of(@NotNull final T... items) {
        requireNonNull(items);
        return CombinationUtil.of(items);
    }

    /**
     * Creates and returns a stream of all possible combinations of the given elements.
     * <p>
     * The order of the combinations in the stream is unspecified.
     *
     * @param <T>   element type
     * @param items a collection of items to combine
     * @return a stream of sets containing all possible combinations of the given elements
     * @throws NullPointerException if the provided {@code items} collection is {@code null}.
     */
    public static <T> Stream<Set<T>> of(@NotNull final Collection<T> items) {
        requireNonNull(items);
        return CombinationUtil.of(items);
    }

    /**
     * Creates and returns a stream of all possible combinations of the given elements.
     * <p>
     * The order of the combinations in the stream is unspecified.
     * <p>
     * It is unspecified if the method lazily consumes the provided stream before providing
     * the result or not.
     *
     * @param <T>   element type
     * @param items a stream of items to combine
     * @return a stream of sets containing all possible combinations of the given elements
     * @throws NullPointerException if the provided {@code items} stream is {@code null}.
     */
    public static <T> Stream<Set<T>> of(@NotNull final Stream<T> items) {
        requireNonNull(items);
        return CombinationUtil.of(items);
    }
}
