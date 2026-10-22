package com.vbforge.stage3;

public class EqualityDemo {

    public static void main(String[] args) {
        Book a = new Book();
        a.title = "The Hobbit";
        a.author = "Tolkien";
        a.pages = 310;

        Book b = new Book();
        b.title = "The Hobbit";
        b.author = "Tolkien";
        b.pages = 310;

        System.out.println("--- Book with overridden equals()/hashCode() ---");
        System.out.println("a == b                       : " + (a == b));                        // false
        System.out.println("a.equals(b)                  : " + a.equals(b));                   // true
        System.out.println("a.hashCode() == b.hashCode() : " + (a.hashCode() == b.hashCode())); // true

        Book c = new Book();
        c.title = "The Hobbit";
        c.author = "Tolkien";
        c.pages = 999;

        System.out.println("\n--- Different pages ---");
        System.out.println("a.equals(c)                  : " + a.equals(c));                   // false
        System.out.println("a.hashCode() == c.hashCode() : " + (a.hashCode() == c.hashCode())); // usually false
    }
}