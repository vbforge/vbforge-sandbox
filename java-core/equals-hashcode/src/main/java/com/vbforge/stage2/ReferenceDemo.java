package com.vbforge.stage2;

public class ReferenceDemo {

    public static void main(String[] args) {
        Book original = new Book();
        original.title = "Effective Java";
        original.author = "Joshua Bloch";
        original.pages = 400;

        // Alias: two variables pointing to the SAME object in memory.
        Book alias = original;

        System.out.println("--- Before change ---");
        System.out.println("original.pages = " + original.pages);
        System.out.println("alias.pages    = " + alias.pages);

        alias.pages = 450;

        System.out.println("\n--- After changing alias ---");
        System.out.println("original.pages = " + original.pages); // also 450!
        System.out.println("alias.pages    = " + alias.pages);

        System.out.println("\noriginal == alias       : " + (original == alias));      // true
        System.out.println("original.equals(alias)  : " + original.equals(alias));     // true
        System.out.println("\nBecause they are the same object, everything is identical.");
    }
}