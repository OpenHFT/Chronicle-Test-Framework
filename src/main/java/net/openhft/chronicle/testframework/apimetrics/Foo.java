package net.openhft.chronicle.testframework.apimetrics;

public class Foo {

    public static final int ORIGO = 0;

    public int x;
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