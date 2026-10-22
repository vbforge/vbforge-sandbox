package com.vbforge.stage2;

public class DefaultEqualityDemo {

    public static void main(String[] args) {
        // Two different objects with identical data.
        Book a = new Book();
        a.title = "The Hobbit";
        a.author = "Tolkien";
        a.pages = 310;

        Book b = new Book();
        b.title = "The Hobbit";
        b.author = "Tolkien";
        b.pages = 310;

        System.out.println("--- Two different objects, same data ---");
        System.out.println("a == b       : " + (a == b));        // false
        System.out.println("a.equals(b)  : " + a.equals(b));   // false  <-- surprise!

        System.out.println("\nWhy? Book does not override equals().");
        System.out.println("So Object.equals() is used, and it behaves like ==.");
    }
}