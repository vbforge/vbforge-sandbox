package com.vbforge.stage4;

public class StringHashDemo {

    public static void main(String[] args) {
        String t1 = "test";
        String t2 = new String("test");

        System.out.println("--- String reference vs content ---");
        System.out.println("t1 == t2                 : " + (t1 == t2));               // false
        System.out.println("t1.equals(t2)            : " + t1.equals(t2));          // true
        System.out.println("t1.hashCode()            : " + t1.hashCode());
        System.out.println("t2.hashCode()            : " + t2.hashCode());
        System.out.println("same hash                : " + (t1.hashCode() == t2.hashCode())); // true

        System.out.println("\nString overrides hashCode() based on content,");
        System.out.println("which is exactly what the contract requires.");
    }
}