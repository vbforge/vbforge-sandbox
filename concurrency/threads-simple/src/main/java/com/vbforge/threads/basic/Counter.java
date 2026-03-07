package com.vbforge.threads.basic;

/**
 * Simple shared counter – used to demonstrate race conditions later.
 */
public class Counter {
    private int count = 0;

    public void increment() {
        count++;
    }

    public int getCount() {
        return count;
    }

    public void reset() {
        count = 0;
    }
}