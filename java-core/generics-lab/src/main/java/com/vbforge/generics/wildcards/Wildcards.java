package com.vbforge.generics.wildcards;

import java.util.List;

/**
 * Demonstrates the three kinds of wildcards in Java Generics:
 *
 * 1. Unbounded wildcard:     List<?>
 * 2. Upper-bounded wildcard: List<? extends T>
 * 3. Lower-bounded wildcard: List<? super T>
 *
 * Classic "PECS" rule reminder:
 *
 * Producer Extends  → use ? extends when you READ from a structure
 * Consumer Super    → use ? super when you WRITE to a structure
 */
public class Wildcards {

    // -------------------------------------------------------------------------
    // 1. Unbounded wildcard: List<?>
    //
    // - Can read elements only as Object
    // - Cannot add elements (except null, but that is rarely useful)
    // -------------------------------------------------------------------------

    public static void printAnyList(List<?> list) {
        for (Object item : list) {
            System.out.println(item);
        }

        // list.add("something"); // Compile-time error
        // list.add(null);        // Technically allowed, but usually avoided
    }

    // -------------------------------------------------------------------------
    // 2. Upper-bounded wildcard: List<? extends Animal>
    //
    // - Safe to GET elements as Animal
    // - Cannot PUT elements because the exact subtype is unknown
    // -------------------------------------------------------------------------

    public static void feedAll(List<? extends Animal> animals) {
        for (Animal animal : animals) {
            animal.eat();
        }

        // animals.add(new Dog());    // Compile-time error
        // animals.add(new Animal()); // Compile-time error
        // animals.add(null);         // Technically allowed but usually avoided
    }

    // -------------------------------------------------------------------------
    // 3. Lower-bounded wildcard: List<? super Dog>
    //
    // - Safe to PUT Dog or its subclasses
    // - Reading is limited to Object
    // -------------------------------------------------------------------------

    public static void adoptDog(Dog dog, List<? super Dog> homes) {

        homes.add(dog);           // OK
        homes.add(new Puppy());   // OK (Puppy extends Dog)

        // homes.add(new Cat());    // Compile-time error
        // homes.add(new Animal()); // Compile-time error

        // Reading is limited to Object
        Object adopted = homes.get(0);
        System.out.println("Adopted animal: " + adopted);
    }

    // -------------------------------------------------------------------------
    // Combined / realistic example using PECS
    //
    // Copy animals from a producer list into a consumer list
    // -------------------------------------------------------------------------

    public static void copyAnimals(
            List<? extends Animal> source,
            List<? super Animal> destination) {

        for (Animal animal : source) {
            destination.add(animal);
        }
    }

    // -------------------------------------------------------------------------
    // Helper classes (small animal hierarchy)
    // -------------------------------------------------------------------------

    public static abstract class Animal {

        public abstract void eat();

        @Override
        public String toString() {
            return getClass().getSimpleName();
        }
    }

    public static class Dog extends Animal {

        @Override
        public void eat() {
            System.out.println("Dog eats bone");
        }
    }

    public static class Puppy extends Dog {

        @Override
        public void eat() {
            System.out.println("Puppy eats milk & bone");
        }
    }

    public static class Cat extends Animal {

        @Override
        public void eat() {
            System.out.println("Cat eats fish");
        }
    }

    // -------------------------------------------------------------------------
// Classic Interview Problem: Swapping elements in List<?>
// -------------------------------------------------------------------------

    /**
     * Swaps two elements in a list.
     *
     * This demonstrates "wildcard capture".
     *
     * List<?> means the element type is unknown, so direct modification
     * is not allowed. We delegate to a helper method that captures the type.
     */
    public static void swap(List<?> list, int i, int j) {
        swapHelper(list, i, j);
    }

    /**
     * Helper method that captures the wildcard type.
     */
    private static <T> void swapHelper(List<T> list, int i, int j) {

        T temp = list.get(i);

        list.set(i, list.get(j));
        list.set(j, temp);
    }
}