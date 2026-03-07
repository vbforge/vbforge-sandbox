package com.vbforge.concurrency.queues;

import java.util.concurrent.*;

public class BlockingQueuePatterns {

    public static void main(String[] args) throws InterruptedException {
        // Producer → Consumer pattern (most common usage)
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(10);

        Runnable producer = () -> {
            try {
                for (int i = 0; i < 25; i++) {
                    String msg = "Task-" + i;
                    queue.put(msg);
                    System.out.println("Produced: " + msg);
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        Runnable consumer = () -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    String msg = queue.take();
                    System.out.println("Consumed: " + msg);
                    Thread.sleep(120);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        ExecutorService executor = Executors.newFixedThreadPool(3);
        executor.submit(producer);
        executor.submit(consumer);
        executor.submit(consumer);

        Thread.sleep(4000);
        executor.shutdownNow();
        executor.awaitTermination(2, TimeUnit.SECONDS);

        System.out.println("Remaining in queue: " + queue.size());
    }
}