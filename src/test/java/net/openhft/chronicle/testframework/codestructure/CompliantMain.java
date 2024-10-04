package net.openhft.chronicle.testframework.codestructure;

public class CompliantMain {

    static {
        DtoAlias.init();
    }

    public static void main(String[] args) {
        System.out.println("Hello world!");
    }
}