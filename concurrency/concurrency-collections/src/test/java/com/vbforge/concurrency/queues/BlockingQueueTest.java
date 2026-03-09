package com.vbforge.concurrency.queues;

import org.junit.jupiter.api.Test;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class BlockingQueueTest {

    @Test
    void producerConsumerShouldTransferAllItems() throws Exception {
        BlockingQueue<Integer> queue = new LinkedBlockingQueue<>(20);
        int tasks = 100;

        ExecutorService executor = Executors.newFixedThreadPool(4);

        executor.submit(() -> {
            try {
                for (int i = 0; i < tasks; i++) {
                    queue.put(i);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        CountDownLatch consumed = new CountDownLatch(tasks);

        for (int i = 0; i < 3; i++) {
            executor.submit(() -> {
                try {
                    while (consumed.getCount() > 0) {
                        queue.take();
                        consumed.countDown();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        assertTrue(consumed.await(5, TimeUnit.SECONDS));
        executor.shutdown();
    }
}