package com.vbforge.generics.type_erasure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Type Erasure in Java Generics - Tests")
class TypeErasureTest {

    @Test
    @DisplayName("Type erasure allows adding wrong type at runtime")
    void shouldAllowAddingWrongTypeDueToErasure() {
        List<Integer> integers = new ArrayList<>();

        TypeErasure.addStringToList(integers);

        assertEquals("secret string", integers.get(0));

        // The next line would throw ClassCastException if uncommented:
        // Integer number = integers.get(0);     // fails at runtime
    }

    @Test
    @DisplayName("instanceof cannot check generic type")
    void cannotCheckGenericTypeWithInstanceof() {
        List<String> strings = new ArrayList<>();
        List<Integer> numbers = new ArrayList<>();

        assertTrue(TypeErasure.isListOfStrings(strings));
        assertTrue(TypeErasure.isListOfStrings(numbers));   // also true!

        // Both return true — erasure removed the distinction
    }

    @Test
    @DisplayName("Using explicit Class token to recover type information")
    void shouldUseClassTokenForRuntimeTypeSafety() {
        var holder = new TypeErasure.GenericHolder<>(String.class);

        holder.set("hello");
        assertTrue(holder.isInstance("world"));
        assertFalse(holder.isInstance(123));

        assertEquals("hello", holder.get());

        // These would throw ClassCastException:
        // holder.set(123);                    // would fail if we enforced it
        // String s = holder.cast(123);        // would throw
    }

    @Test
    @DisplayName("Safe cast using Class token")
    void shouldPerformSafeCastWithClassToken() {
        Object value = "I'm actually a String";

        String s = TypeErasure.safeCast(value, String.class);
        assertEquals("I'm actually a String", s);

        assertThrows(ClassCastException.class, () ->
                TypeErasure.safeCast(123, String.class));
    }

    @Test
    @DisplayName("Array creation with generics – safe way")
    void shouldDemonstrateSafeArrayCreation() {
        // Safe way: uses reflection for correct type
        Integer[] good = TypeErasure.createArrayGoodWay(Integer.class, 4);
        good[0] = 42;

        assertEquals(42, good[0]);
        // No exceptions, correct type
    }

    @Test
    @DisplayName("Array creation with generics – unsafe vs safe way")
    void shouldDemonstrateArrayCreationPatterns() {
        // Unsafe way: may throw ClassCastException immediately
        try {
            String[] unsafe = TypeErasure.createArrayBadWay(String.class, 3);
            fail("Expected ClassCastException on array creation");
        } catch (ClassCastException e) {
            // Expected: [Ljava.lang.Object; cannot be cast to [Ljava.lang.String;
        }

        // Safe way: uses reflection for correct type
        Integer[] good = TypeErasure.createArrayGoodWay(Integer.class, 4);
        good[0] = 42;

        assertEquals(42, good[0]);
    }

}