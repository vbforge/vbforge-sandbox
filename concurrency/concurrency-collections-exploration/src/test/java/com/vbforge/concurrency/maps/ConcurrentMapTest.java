package com.vbforge.concurrency.maps;

import org.junit.jupiter.api.Test;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class ConcurrentMapTest {

    @Test
    void shouldCorrectlyCountUnderConcurrency() throws Exception {
        ConcurrentHashMap<String, Long> map = new ConcurrentHashMap<>();

        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(10);

        for (int i = 0; i < 10; i++) {
            executor.submit(() -> {
                for (int j = 0; j < 1000; j++) {
                    map.compute("counter", (k, v) -> v == null ? 1L : v + 1);
                }
                latch.countDown();
            });
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        executor.shutdown();

        assertEquals(10_000L, map.get("counter").longValue());
    }
}