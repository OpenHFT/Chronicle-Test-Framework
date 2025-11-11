/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.testframework.internal.apimetrics;

class Foo {

    public static final int ORIGO = 0;

    private int x;
    protected int y;

    public int x() {
        return x;
    }

    public void x(int x) {
        this.x = x;
    }

    protected int zero() {
        return 0;
    }

}
