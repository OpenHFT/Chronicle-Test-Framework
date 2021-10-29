package net.openhft.chronicle.testframework;

/**
 * This class is just a demo of the features of this library and may be changed or removed for any reason.
 */
public class Demo {

    public static void main(String[] args) {
        System.out.println("Combinations:");
        Combination.of("A", "B", "C")
                .forEach(System.out::println);

        System.out.println("Permutations:");
        Permutation.of("A", "B", "C")
                .forEach(System.out::println);

    }

}
