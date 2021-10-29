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

import net.openhft.chronicle.testframework.Combination;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Per Minborg
 */
final class CombinationDemo {

    @Test
    void print() {
        Combination.of("A", "B", "C")
                .forEach(System.out::println);
    }

    @TestFactory
    // Try all combinations of cosmic ray interference
    Stream<DynamicTest> demo() {
        return DynamicTest.stream(Combination.<Consumer<FaultTolerantBitSet>>of(
                        FaultTolerantBitSet::cosmicRayBit3,
                        FaultTolerantBitSet::cosmicRayBit23,
                        FaultTolerantBitSet::cosmicRayBit13),
                Objects::toString,
                operations -> {
                    final FaultTolerantBitSet bitSet = new FaultTolerantBitSet();
                    operations.forEach(oper -> oper.accept(bitSet));
                    assertTrue(bitSet.isValid());
                });
    }

    private static class FaultTolerantBitSet {

        public void cosmicRayBit3() {
        }

        public void cosmicRayBit23() {
        }

        public void cosmicRayBit13() {
        }


        boolean isValid() {
            return true;
        }

    }


}

