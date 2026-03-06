package com.vbforge.generics.bounded_types;

import java.util.List;

/**
 * Examples of bounded type parameters in Java Generics.
 * Upper bounds (<T extends ...>) and lower bounds (super) are demonstrated.
 */
public class BoundedTypes {

    // === Upper Bounded Types (most common) ===

    /**
     * Accepts any type that is Number or its subclass (Integer, Double, Float, BigDecimal...)
     */
    public static double sumOfNumbers(NumberBox<? extends Number> box) {
        Number value = box.getValue();
        return value.doubleValue();
    }

    /**
     * More strict version - only allows Number subclasses that implement Comparable
     */
    public static <T extends Number & Comparable<T>> T maxOf(T a, T b) {
        return a.compareTo(b) >= 0 ? a : b;
    }

    // === Lower Bounded Types (less common but very useful) ===

    /**
     * Can accept Integer, Number, Object, Serializable... (anything Integer is assignable to)
     */
    public static void addIntegerToList(Integer value, List<? super Integer> list) {
        list.add(value);
        // list.add(3.14);     // ← compile error - good!
        // list.add(new Object()); // ← compile error - good!
    }

    // === Combined example - Animal hierarchy ===

    public static void feedAnimals(List<? extends Animal> animals) {
        for (Animal animal : animals) {
            animal.eat();
        }
        // animals.add(new Dog());   // ← compile error - good!
    }

    public static void adoptAnimal(Animal animal, List<? super Dog> homes) {
        homes.add(new Dog());           // OK
        // homes.add(new Cat());        // compile error
        // homes.add(new Animal());     // compile error
    }

    // Helper classes for the Animal example
    public static abstract class Animal {
        public abstract void eat();
    }

    public static class Dog extends Animal {
        @Override
        public void eat() {
            System.out.println("Dog eats bones");
        }
    }

    public static class Cat extends Animal {
        @Override
        public void eat() {
            System.out.println("Cat eats fish");
        }
    }

    // === Utility class with bounded type ===

    public static class NumberBox<T extends Number> {
        private final T value;

        public NumberBox(T value) {
            this.value = value;
        }

        public T getValue() {
            return value;
        }

        public double doubleValue() {
            return value.doubleValue();
        }
    }
}
