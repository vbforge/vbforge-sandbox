package com.vbforge.stage1;

public class StringComparison {

    public static void main(String[] args) {
        // String literals are interned: the JVM reuses the same object.
        String title1 = "Clean Code";
        String title2 = "Clean Code";

        System.out.println("--- Two string literals with the same text ---");
        System.out.println("title1 == title2       : " + (title1 == title2));   // true
        System.out.println("title1.equals(title2)  : " + title1.equals(title2)); // true

        // new String() always creates a fresh object, even if the text is identical.
        String title3 = new String("Clean Code");

        System.out.println("\n--- Literal vs new String(...) ---");
        System.out.println("title1 == title3       : " + (title1 == title3));   // false
        System.out.println("title1.equals(title3)  : " + title1.equals(title3)); // true

        System.out.println("\nLesson:");
        System.out.println("== compares references (where the object lives).");
        System.out.println("equals() compares content (for String, character by character).");
    }
}