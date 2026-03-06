package com.vbforge.generics.generic_interfaces;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Generic Interfaces - Tests")
class GenericInterfacesTest {

    @Test
    @DisplayName("Non-generic implementation of generic interface")
    void stringBoxShouldWorkAsContainer() {
        GenericInterfaces.Container<String> box = new GenericInterfaces.StringBox();

        assertTrue(box.isEmpty());

        box.add("Hello Generics");
        assertFalse(box.isEmpty());
        assertEquals("Hello Generics", box.get());

        box.add("New value");
        assertEquals("New value", box.get());  // overwrites
    }

    @Test
    @DisplayName("Generic implementation of generic interface")
    void genericBoxShouldSupportAnyType() {
        GenericInterfaces.Container<Integer> intBox = new GenericInterfaces.GenericBox<>();
        GenericInterfaces.Container<Person> personBox = new GenericInterfaces.GenericBox<>();

        intBox.add(42);
        personBox.add(new Person("Anna", 29));

        assertEquals(42, intBox.get());
        assertEquals("Anna", personBox.get().name);
    }

    @Test
    @DisplayName("Bounded generic interface - Measurable")
    void shouldCompareWeights() {
        var w1 = new GenericInterfaces.Weight(65.5);
        var w2 = new GenericInterfaces.Weight(72.0);
        var w3 = new GenericInterfaces.Weight(65.5);

        assertTrue(w2.isGreaterThan(w1));
        assertFalse(w1.isGreaterThan(w2));
        assertEquals(0, w1.compareTo(w3));

        // This would not compile:
        // var invalid = new GenericInterfaces.Weight("65 kg"); // type mismatch
    }

    @Test
    @DisplayName("Multiple type parameters - Mapper / SimpleCache")
    void simpleCacheShouldBehaveLikeMap() {
        GenericInterfaces.Mapper<String, Integer> cache = new GenericInterfaces.SimpleCache<>();

        assertEquals(0, cache.size());
        assertFalse(cache.containsKey("age"));

        cache.put("age", 35);
        cache.put("score", 92);

        assertEquals(2, cache.size());
        assertTrue(cache.containsKey("age"));
        assertEquals(35, cache.get("age"));
        assertNull(cache.get("name"));
    }

    // Helper record (Java 17+)
    private record Person(String name, int age) {}
}