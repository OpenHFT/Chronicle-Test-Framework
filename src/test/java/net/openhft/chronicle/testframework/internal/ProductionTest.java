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

import net.openhft.chronicle.testframework.Product;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class ProductionTest {

    @Test
    void product2() {
        final List<String> strings = Arrays.asList("A", "B", "C");
        final List<Integer> integers = Arrays.asList(1, 2, 3);
        final List<Product.Product2<String, Integer>> actual =
                Product.of(strings, integers)
                        .collect(toList());

        final List<Product.Product2<String, Integer>> expected = new ArrayList<>();
        for (String s : strings) {
            for (Integer i : integers) {
                expected.add(new ProductUtil.Product2Impl<>(s, i));
            }
        }
        assertEquals(expected, actual);
    }

    @Test
    void product3() {
        final List<String> strings = Arrays.asList("A", "B", "C");
        final List<Integer> integers = Arrays.asList(1, 2, 3);
        final List<Long> longs = Arrays.asList(10L, 20L, 30L);
        final List<Product.Product3<String, Integer, Long>> actual =
                Product.of(strings, integers, longs)
                        .collect(toList());

        final List<Product.Product3<String, Integer, Long>> expected = new ArrayList<>();
        for (String s : strings) {
            for (Integer i : integers) {
                for (Long l:longs) {
                    expected.add(new ProductUtil.Product3Impl<>(s, i, l));
                }
            }
        }
        assertEquals(expected, actual);
    }


}