package com.vbforge.stage4;

import com.vbforge.stage3.Book;

import java.util.HashSet;
import java.util.Set;

public class HashSetDemo {

    public static void main(String[] args) {
        Set<Book> books = new HashSet<>();

        Book a = new Book();
        a.title = "The Hobbit";
        a.author = "Tolkien";
        a.pages = 310;

        Book b = new Book();
        b.title = "The Hobbit";
        b.author = "Tolkien";
        b.pages = 310;

        books.add(a);
        books.add(b);

        System.out.println("books.size() = " + books.size()); // 1!
        System.out.println("\nBecause Book overrides equals() AND hashCode(),");
        System.out.println("HashSet treats a and b as duplicates and keeps only one.");
    }
}