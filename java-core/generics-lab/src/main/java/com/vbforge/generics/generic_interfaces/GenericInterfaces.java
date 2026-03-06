package com.vbforge.generics.generic_interfaces;

/**
 * Demonstrates generic interfaces in Java:
 * - Basic generic interface
 * - Interface with bounded type parameter
 * - Multiple generic parameters
 * - Generic interface implemented by both generic & non-generic classes
 */
public class GenericInterfaces {

    // === Basic generic interface ===

    public interface Container<T> {
        void add(T item);
        T get();
        boolean isEmpty();
    }

    // === Bounded generic interface ===

    public interface Measurable<T extends Number & Comparable<T>> {
        T getValue();
        default int compareTo(Measurable<T> other) {
            return getValue().compareTo(other.getValue());
        }
        default boolean isGreaterThan(Measurable<T> other) {
            return compareTo(other) > 0;
        }
    }

    // === Interface with multiple type parameters ===

    public interface Mapper<K, V> {
        void put(K key, V value);
        V get(K key);
        boolean containsKey(K key);
        int size();
    }

    // === Concrete implementations ===

    // 1. Non-generic class implementing generic interface (fixed type)
    public static class StringBox implements Container<String> {
        private String value;

        @Override
        public void add(String item) {
            this.value = item;
        }

        @Override
        public String get() {
            return value;
        }

        @Override
        public boolean isEmpty() {
            return value == null;
        }
    }

    // 2. Generic class implementing generic interface
    public static class GenericBox<T> implements Container<T> {
        private T value;

        @Override
        public void add(T item) {
            this.value = item;
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public boolean isEmpty() {
            return value == null;
        }
    }

    // 3. Class implementing bounded generic interface
    public static class Weight implements Measurable<Double> {
        private final double kilograms;

        public Weight(double kilograms) {
            this.kilograms = kilograms;
        }

        @Override
        public Double getValue() {
            return kilograms;
        }

        @Override
        public String toString() {
            return kilograms + " kg";
        }
    }

    // 4. Generic class implementing multi-parameter interface
    public static class SimpleCache<K, V> implements Mapper<K, V> {
        private final java.util.Map<K, V> map = new java.util.HashMap<>();

        @Override
        public void put(K key, V value) {
            map.put(key, value);
        }

        @Override
        public V get(K key) {
            return map.get(key);
        }

        @Override
        public boolean containsKey(K key) {
            return map.containsKey(key);
        }

        @Override
        public int size() {
            return map.size();
        }
    }
}