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

import net.openhft.chronicle.testframework.Permutation;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PermutationDemo {

    @Test
    void print() {
        Permutation.of("A", "B", "C")
                .forEach(System.out::println);
    }

    @TestFactory
    // Exhaustively tests that the order of setter invocation does not matter
    Stream<DynamicTest> demo() {
        // Operations
        final Consumer<MyBean> setA = myBean -> myBean.setA(1);
        final Consumer<MyBean> setB = myBean -> myBean.setB(2);
        final Consumer<MyBean> setC = myBean -> myBean.setC(3);
        final MyBean expected = new MyBean(1, 2, 3);
        // DynamicTests
        return DynamicTest.stream(Permutation.of(setA, setB, setC),
                Objects::toString,
                operations -> {
                    final MyBean actual = new MyBean();
                    operations.forEach(oper -> oper.accept(actual));
                    assertEquals(expected, actual);
                });
    }


    static final class MyBean {

        int a;
        int b;
        int c;

        public MyBean() {
        }

        public MyBean(int a, int b, int c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        public int getA() {
            return a;
        }

        public void setA(int a) {
            this.a = a;
        }

        public int getB() {
            return b;
        }

        public void setB(int b) {
            this.b = b;
        }

        public int getC() {
            return c;
        }

        public void setC(int c) {
            this.c = c;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MyBean myBean = (MyBean) o;

            if (a != myBean.a) return false;
            if (b != myBean.b) return false;
            return c == myBean.c;
        }

        @Override
        public int hashCode() {
            int result = a;
            result = 31 * result + b;
            result = 31 * result + c;
            return result;
        }

        @Override
        public String toString() {
            return "MyBean{" +
                    "a=" + a +
                    ", b=" + b +
                    ", c=" + c +
                    '}';
        }
    }

}