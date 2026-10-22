package com.vbforge.stage2;

public class DefaultHashCodeDemo {

    public static void main(String[] args) {
        Book a = new Book();
        Book b = new Book();
        Book c = a; // alias — same object as a

        System.out.println("a.hashCode() = " + a.hashCode());
        System.out.println("b.hashCode() = " + b.hashCode());
        System.out.println("c.hashCode() = " + c.hashCode());

        System.out.println("\na == c                     : " + (a == c));                          // true
        System.out.println("a.hashCode() == c.hashCode(): " + (a.hashCode() == c.hashCode()));  // true
        System.out.println("a.hashCode() == b.hashCode(): " + (a.hashCode() == b.hashCode()));  // usually false

        System.out.println("\nThe default hashCode() is identity-based (not a memory address,");
        System.out.println("but behaves as if each object has its own number).");
    }
}