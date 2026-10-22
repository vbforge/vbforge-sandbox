package com.vbforge.stage3;

public class ManualEqualityDemo {

    public static void main(String[] args) {
        ManualBook x = new ManualBook();
        x.title = "Dune";
        x.author = "Herbert";
        x.pages = 500;

        ManualBook y = new ManualBook();
        y.title = "Dune";
        y.author = "Herbert";
        y.pages = 500;

        System.out.println("--- Manual implementation ---");
        System.out.println("x.equals(y)                  : " + x.equals(y));
        System.out.println("x.hashCode() == y.hashCode() : " + (x.hashCode() == y.hashCode()));

        System.out.println("\nContract checks:");
        System.out.println("reflexive   x.equals(x)          : " + x.equals(x));
        System.out.println("null-safe   x.equals(null)       : " + x.equals(null));
        System.out.println("symmetric   x.equals(y)==y.equals(x): " + (x.equals(y) == y.equals(x)));
    }
}