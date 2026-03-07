package com.vbforge.threads.synchronization;

/**
 * Thread-safe counter using synchronized methods (Java 17 classic approach).
 */
public class SynchronizedCounter {
    private int count = 0;

    public synchronized void increment() {
        count++;
    }

    public synchronized int getCount() {
        return count;
    }
}