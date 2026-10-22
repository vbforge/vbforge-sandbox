package com.vbforge.stage3;

/**
 * Same behavior as Book, but written manually
 * so you can see what "Objects.equals / Objects.hash" actually do.
 */
public class ManualBook {
    public String title;
    public String author;
    public int pages;

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;                     // null check
        if (other == this) return true;                      // reflexivity
        if (other.getClass() != ManualBook.class) return false; // exact type

        ManualBook that = (ManualBook) other;
        if (this.pages != that.pages) return false;
        if (!this.title.equals(that.title)) return false;
        if (!this.author.equals(that.author)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        // Simple, valid, hand-written combination.
        int result = title.hashCode();
        result = 31 * result + author.hashCode();
        result = 31 * result + pages;
        return result;
    }
}