package com.vbforge.generics.bounded_types;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.vbforge.generics.bounded_types.BoundedTypes.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Bounded Types in Generics - Tests")
class BoundedTypesTest {

    @Test
    @DisplayName("Upper bound: ? extends Number")
    void shouldSumNumbersWithUpperBound() {
        var intBox = new BoundedTypes.NumberBox<>(42);
        var doubleBox = new BoundedTypes.NumberBox<>(3.14);
        var longBox = new BoundedTypes.NumberBox<>(1000L);

        double sum = sumOfNumbers(intBox)
                + sumOfNumbers(doubleBox)
                + sumOfNumbers(longBox);

        assertEquals(1045.14, sum, 0.001);
    }

    @Test
    @DisplayName("Multiple bounds: Number & Comparable")
    void shouldFindMaxWithMultipleBounds() {
        Integer maxInt = maxOf(10, 25);
        Double maxDouble = maxOf(3.5, 2.8);

        assertEquals(25, maxInt);
        assertEquals(3.5, maxDouble, 0.001);

        // String would NOT compile:
        // BoundedTypes.maxOf("abc", "def");  // compile error - good!
    }

    @Test
    @DisplayName("Lower bound: ? super Integer")
    void shouldAddToLowerBoundedList() {
        List<Number> numbers = new ArrayList<>();
        List<Object> objects = new ArrayList<>();

        addIntegerToList(100, numbers);
        addIntegerToList(200, objects);

        assertTrue(numbers.contains(100));
        assertTrue(objects.contains(200));
    }

    @Test
    @DisplayName("PECS - Producer Extends, Consumer Super (Animal example)")
    void shouldDemonstratePECS() {
        // Producer → extends
        List<Cat> cats = Arrays.asList(new Cat(), new Cat());
        List<Dog> dogs = Arrays.asList(new Dog(), new Dog());

        feedAnimals(cats);     // OK
        feedAnimals(dogs);     // OK
        // BoundedTypes.feedAnimals(List.of("string")); // compile error - good!

        // Consumer → super
        List<Animal> animals = new ArrayList<>();
        List<Dog> onlyDogs = new ArrayList<>();
        List<Object> objects = new ArrayList<>();

        adoptAnimal(new Dog(), animals);
        adoptAnimal(new Dog(), onlyDogs);
        adoptAnimal(new Dog(), objects);

        assertEquals(3, animals.size() + onlyDogs.size() + objects.size());
    }
}