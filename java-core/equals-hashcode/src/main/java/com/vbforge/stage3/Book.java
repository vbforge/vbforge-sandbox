package com.vbforge.stage3;

import java.util.Objects;

public class Book {
    public String title;
    public String author;
    public int pages;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;                                    // reflexivity + fast path
        if (o == null || getClass() != o.getClass()) return false;     // null and type check
        Book other = (Book) o;                                         // safe cast
        return pages == other.pages
                && Objects.equals(title, other.title)
                && Objects.equals(author, other.author);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, author, pages);
    }
}