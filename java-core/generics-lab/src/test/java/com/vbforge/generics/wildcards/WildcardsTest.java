package com.vbforge.generics.wildcards;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Wildcards (? extends / ? super) - Tests")
class WildcardsTest {

    @Test
    @DisplayName("Unbounded wildcard: List<?> – can read elements as Object")
    void unboundedWildcardAllowsReading() {

        List<String> strings = new ArrayList<>(List.of("A", "B", "C"));
        List<Integer> numbers = new ArrayList<>(List.of(10, 20));

        assertDoesNotThrow(() -> Wildcards.printAnyList(strings));
        assertDoesNotThrow(() -> Wildcards.printAnyList(numbers));

        assertEquals(3, strings.size());
        assertEquals(2, numbers.size());
    }

    @Test
    @DisplayName("? extends Animal → producer: can read but not write")
    void upperBoundedWildcardIsProducerSafe() {

        List<Wildcards.Dog> dogs = new ArrayList<>();
        dogs.add(new Wildcards.Dog());
        dogs.add(new Wildcards.Puppy());

        List<Wildcards.Cat> cats = new ArrayList<>();
        cats.add(new Wildcards.Cat());

        assertDoesNotThrow(() -> Wildcards.feedAll(dogs));
        assertDoesNotThrow(() -> Wildcards.feedAll(cats));

        assertEquals(2, dogs.size());
        assertEquals(1, cats.size());
    }

    @Test
    @DisplayName("? super Dog → consumer: can write Dog/Puppy")
    void lowerBoundedWildcardIsConsumerSafe() {

        List<Wildcards.Animal> animals = new ArrayList<>();
        List<Wildcards.Dog> onlyDogs = new ArrayList<>();
        List<Object> objects = new ArrayList<>();

        Wildcards.Dog rex = new Wildcards.Dog();
        Wildcards.Puppy puppy = new Wildcards.Puppy();

        Wildcards.adoptDog(rex, animals);
        Wildcards.adoptDog(puppy, onlyDogs);
        Wildcards.adoptDog(rex, objects);

        assertEquals(2, animals.size());
        assertEquals(2, onlyDogs.size());
        assertEquals(2, objects.size());

        Object adopted = animals.get(0);
        assertTrue(adopted instanceof Wildcards.Dog);
    }

    @Test
    @DisplayName("PECS example: copy from ? extends → to ? super")
    void shouldCopyUsingPECSPattern() {

        List<Wildcards.Dog> sourceDogs = new ArrayList<>();
        sourceDogs.add(new Wildcards.Dog());
        sourceDogs.add(new Wildcards.Puppy());

        List<Wildcards.Animal> destination = new ArrayList<>();

        Wildcards.copyAnimals(sourceDogs, destination);

        assertEquals(2, destination.size());
        assertTrue(destination.get(0) instanceof Wildcards.Dog);
        assertTrue(destination.get(1) instanceof Wildcards.Puppy);

        // Reverse would not compile
        // Wildcards.copyAnimals(destination, sourceDogs);
    }

    @Test
    @DisplayName("Wildcard capture: swap elements in String list")
    void shouldSwapElementsInStringList() {

        List<String> list = new ArrayList<>(List.of("A", "B", "C"));

        Wildcards.swap(list, 0, 2);

        assertEquals("C", list.get(0));
        assertEquals("B", list.get(1));
        assertEquals("A", list.get(2));
    }

    @Test
    @DisplayName("Wildcard capture: swap elements in Integer list")
    void shouldSwapElementsInIntegerList() {

        List<Integer> numbers = new ArrayList<>(List.of(1, 2, 3, 4));

        Wildcards.swap(numbers, 1, 3);

        assertEquals(List.of(1, 4, 3, 2), numbers);
    }

}