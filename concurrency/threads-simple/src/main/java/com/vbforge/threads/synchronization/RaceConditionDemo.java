package com.vbforge.threads.synchronization;

import com.vbforge.threads.basic.Counter;

/**
 * Shows what happens WITHOUT synchronization (race condition).
 */
public class RaceConditionDemo {
    public static void main(String[] args) throws InterruptedException {
        Counter counter = new Counter();

        Runnable task = () -> {
            for (int i = 0; i < 1000; i++) {
                counter.increment();   // NOT thread-safe
            }
        };

        Thread t1 = new Thread(task, "Worker-1");
        Thread t2 = new Thread(task, "Worker-2");

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println("Final count WITHOUT sync (should be 2000): " + counter.getCount());
        // You will often see < 2000 → race condition!
    }
}