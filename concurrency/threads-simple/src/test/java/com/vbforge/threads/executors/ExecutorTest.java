package com.vbforge.threads.executors;

import org.junit.jupiter.api.Test;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class ExecutorTest {

    @Test
    void futureShouldReturnCorrectValue() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<Integer> future = executor.submit(() -> 42);

        assertEquals(42, future.get(1, TimeUnit.SECONDS));

        executor.shutdown();
    }
}