package com.vbforge.stage3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the five properties of the equals() contract and the
 * hashCode() contract for {@link Book} (the "Objects.equals / Objects.hash" version).
 *
 * The five properties of equals():
 *   1. Reflexivity   — x.equals(x) is true
 *   2. Symmetry      — x.equals(y) iff y.equals(x)
 *   3. Transitivity  — x=y and y=z implies x=z
 *   4. Consistency   — repeated calls return the same result
 *   5. Null-safety   — x.equals(null) is false
 */
@DisplayName("Book — equals() and hashCode() contract")
class BookEqualityTest {

    private Book cleanCode;         // x
    private Book cleanCodeTwin;     // y  (equal to x)
    private Book cleanCodeThird;    // z  (equal to x and y)
    private Book effectiveJava;     // unrelated book

    @BeforeEach
    void setUp() {
        cleanCode      = book("Clean Code", "Robert C. Martin", 464);
        cleanCodeTwin  = book("Clean Code", "Robert C. Martin", 464);
        cleanCodeThird = book("Clean Code", "Robert C. Martin", 464);
        effectiveJava  = book("Effective Java", "Joshua Bloch", 412);
    }

    private static Book book(String title, String author, int pages) {
        Book b = new Book();
        b.title = title;
        b.author = author;
        b.pages = pages;
        return b;
    }

    // ---------------------------------------------------------------
    // 1. Reflexivity
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("1. Reflexivity")
    class Reflexivity {

        @Test
        @DisplayName("x.equals(x) must return true")
        void objectEqualsItself() {
            assertTrue(cleanCode.equals(cleanCode));
        }

        @Test
        @DisplayName("an unequal book is also reflexive")
        void unrelatedBookIsReflexive() {
            assertTrue(effectiveJava.equals(effectiveJava));
        }
    }

    // ---------------------------------------------------------------
    // 2. Symmetry
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("2. Symmetry")
    class Symmetry {

        @Test
        @DisplayName("if x.equals(y) then y.equals(x)")
        void equalBooksAreSymmetric() {
            assertAll(
                () -> assertTrue(cleanCode.equals(cleanCodeTwin)),
                () -> assertTrue(cleanCodeTwin.equals(cleanCode))
            );
        }

        @Test
        @DisplayName("if x does not equal y, y does not equal x either")
        void unequalBooksAreSymmetric() {
            assertAll(
                () -> assertFalse(cleanCode.equals(effectiveJava)),
                () -> assertFalse(effectiveJava.equals(cleanCode))
            );
        }
    }

    // ---------------------------------------------------------------
    // 3. Transitivity
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("3. Transitivity")
    class Transitivity {

        @Test
        @DisplayName("x.equals(y) and y.equals(z) implies x.equals(z)")
        void equalBooksAreTransitive() {
            assertAll(
                () -> assertTrue(cleanCode.equals(cleanCodeTwin)),
                () -> assertTrue(cleanCodeTwin.equals(cleanCodeThird)),
                () -> assertTrue(cleanCode.equals(cleanCodeThird))   // must follow
            );
        }

        @Test
        @DisplayName("a broken chain still behaves correctly")
        void brokenChainStaysConsistent() {
            assertAll(
                () -> assertTrue(cleanCode.equals(cleanCodeTwin)),
                () -> assertFalse(cleanCodeTwin.equals(effectiveJava)),
                () -> assertFalse(cleanCode.equals(effectiveJava))
            );
        }
    }

    // ---------------------------------------------------------------
    // 4. Consistency
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("4. Consistency")
    class Consistency {

        @Test
        @DisplayName("repeated calls to equals() return the same result")
        void repeatedCallsAreStable() {
            // No fields are mutated between calls — the answer must not change.
            for (int i = 0; i < 100; i++) {
                assertTrue(cleanCode.equals(cleanCodeTwin),
                        "equal books must stay equal across repeated calls");
                assertFalse(cleanCode.equals(effectiveJava),
                        "unequal books must stay unequal across repeated calls");
            }
        }
    }

    // ---------------------------------------------------------------
    // 5. Null-safety
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("5. Null-safety")
    class NullSafety {

        @Test
        @DisplayName("x.equals(null) must return false")
        void equalsNullIsFalse() {
            assertFalse(cleanCode.equals(null));
        }
    }

    // ---------------------------------------------------------------
    // Bonus: field-level correctness
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("Field-level correctness")
    class FieldCorrectness {

        @Test
        @DisplayName("differing title makes books unequal")
        void differentTitle() {
            Book other = book("The Pragmatic Programmer", "Robert C. Martin", 464);
            assertNotEquals(cleanCode, other);
        }

        @Test
        @DisplayName("differing author makes books unequal")
        void differentAuthor() {
            Book other = book("Clean Code", "Someone Else", 464);
            assertNotEquals(cleanCode, other);
        }

        @Test
        @DisplayName("differing pages makes books unequal")
        void differentPages() {
            Book other = book("Clean Code", "Robert C. Martin", 465);
            assertNotEquals(cleanCode, other);
        }
    }

    // ---------------------------------------------------------------
    // hashCode() contract
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("hashCode() contract")
    class HashCodeContract {

        @Test
        @DisplayName("equal objects must have equal hash codes")
        void equalObjectsShareHashCode() {
            assertEquals(cleanCode.hashCode(), cleanCodeTwin.hashCode(),
                    "if a.equals(b) then a.hashCode() == b.hashCode()");
        }

        @Test
        @DisplayName("hashCode() is consistent across calls")
        void hashCodeIsConsistent() {
            int first = cleanCode.hashCode();
            for (int i = 0; i < 100; i++) {
                assertEquals(first, cleanCode.hashCode(),
                        "hashCode must not change while fields are unchanged");
            }
        }

        @Test
        @DisplayName("unequal objects MAY share a hash code (collision is allowed)")
        void unequalObjectsCanCollide() {
            // Known Java fact: "Aa".hashCode() == "BB".hashCode() == 2112.
            // Because Book.hashCode() combines field hashes, these two books collide.
            Book a = book("Aa", "Author", 100);
            Book b = book("BB", "Author", 100);

            assertAll(
                () -> assertNotEquals(a.title, b.title),
                () -> assertNotEquals(a, b, "different fields -> not equal"),
                () -> assertEquals(a.hashCode(), b.hashCode(),
                        "same hash does NOT imply equal — collisions are legal")
            );
        }
    }

    // ---------------------------------------------------------------
    // Practical impact: HashSet relies on BOTH methods
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("Integration: HashSet")
    class HashSetIntegration {

        @Test
        @DisplayName("HashSet keeps only one of two equal books")
        void hashSetDeduplicatesEqualBooks() {
            Set<Book> shelf = new HashSet<>();
            shelf.add(book("Clean Code", "Robert C. Martin", 464));
            shelf.add(book("Clean Code", "Robert C. Martin", 464));
            shelf.add(book("Effective Java", "Joshua Bloch", 412));

            assertEquals(2, shelf.size(),
                    "duplicates must be removed via equals() + hashCode()");
        }
    }
}