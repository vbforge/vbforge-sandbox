package com.vbforge.threads.executors;

import java.util.concurrent.*;

/**
 * Modern way to handle threads in Java 17: ExecutorService + thread pools.
 * Never create raw "new Thread()" in production!
 */
public class ThreadPoolDemo {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(4);

        try {
            // Submit many tasks
            for (int i = 0; i < 10; i++) {
                final int taskId = i;
                executor.submit(() -> {
                    System.out.println("Task " + taskId + " by " + Thread.currentThread().getName());
                    try { Thread.sleep(200); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                });
            }

            // Callable + Future example
            Future<Integer> future = executor.submit(() -> {
                Thread.sleep(300);
                return 42;
            });

            System.out.println("Future result: " + future.get());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
    }
}