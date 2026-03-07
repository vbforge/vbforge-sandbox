package com.vbforge.threads.synchronization;

import org.junit.jupiter.api.Test;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

class SynchronizationTest {

    @Test
    void synchronizedCounterShouldBeCorrect() throws InterruptedException {
        SynchronizedCounter counter = new SynchronizedCounter();
        ExecutorService executor = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 1000; i++) {
            executor.submit(counter::increment);
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        assertEquals(1000, counter.getCount(), "Counter should reach exactly 1000");
    }
}