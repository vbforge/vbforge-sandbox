package com.vbforge.concurrency.lists;

import org.junit.jupiter.api.Test;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class CopyOnWriteListTest {

    @Test
    void iterationSafeWhileWriting() throws Exception {

        CopyOnWriteArrayList<Integer> list = new CopyOnWriteArrayList<>();

        // Pre-fill list so readers actually iterate
        for (int i = 0; i < 100; i++) {
            list.add(i);
        }

        AtomicInteger iterations = new AtomicInteger(0);

        Runnable reader = () -> {
            for (int ignored : list) {
                iterations.incrementAndGet();
            }
        };

        ExecutorService executor = Executors.newFixedThreadPool(4);

        // start readers
        for (int i = 0; i < 3; i++) {
            executor.submit(reader);
        }

        Thread.sleep(100);

        // writer
        for (int i = 0; i < 500; i++) {
            list.add(i);
        }

        executor.shutdown();
        executor.awaitTermination(3, TimeUnit.SECONDS);

        assertTrue(iterations.get() > 0);
        assertEquals(600, list.size());
    }
}