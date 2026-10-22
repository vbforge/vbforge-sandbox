package com.vbforge.stage3;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Confirms that the hand-written equals()/hashCode() in {@link ManualBook}
 * satisfies the same five properties as the Objects.equals/Objects.hash version.
 */
@DisplayName("ManualBook — the hand-written contract holds")
class ManualBookEqualityTest {

    private static ManualBook book(String title, String author, int pages) {
        ManualBook b = new ManualBook();
        b.title = title;
        b.author = author;
        b.pages = pages;
        return b;
    }

    @Test
    @DisplayName("All five equals() properties hold for the manual implementation")
    void allFivePropertiesHold() {
        ManualBook x = book("Dune", "Herbert", 500);
        ManualBook y = book("Dune", "Herbert", 500);
        ManualBook z = book("Dune", "Herbert", 500);
        ManualBook other = book("Dune", "Herbert", 999);

        assertAll("equals() contract",
                () -> assertTrue(x.equals(x),                         "1. reflexivity"),
                () -> assertTrue(x.equals(y) && y.equals(x),          "2. symmetry (equal)"),
                () -> assertFalse(x.equals(other) || other.equals(x), "2. symmetry (unequal)"),
                () -> assertTrue(x.equals(y) && y.equals(z) && x.equals(z), "3. transitivity"),
                () -> {
                    boolean first = x.equals(y);
                    for (int i = 0; i < 100; i++) {
                        assertEquals(first, x.equals(y), "4. consistency");
                    }
                },
                () -> assertFalse(x.equals(null),                     "5. null-safety")
        );

        assertEquals(x.hashCode(), y.hashCode(),
                "hashCode contract: equal objects must have equal hashes");
    }
}