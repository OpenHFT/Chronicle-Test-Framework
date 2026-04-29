/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.testframework.codestructure;

public class CompliantMain {

    static {
        DtoAlias.init();
    }

    public static void main(String[] args) {
        System.out.println("Hello world!");
    }
}