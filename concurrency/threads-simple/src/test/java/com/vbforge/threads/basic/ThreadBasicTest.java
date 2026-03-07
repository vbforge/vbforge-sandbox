package com.vbforge.threads.basic;

import org.junit.jupiter.api.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

class ThreadBasicTest {

    @Test
    void threeThreadsShouldComplete() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(3);

        Thread t1 = new Thread(latch::countDown);
        Thread t2 = new Thread(latch::countDown);
        Thread t3 = new Thread(latch::countDown);

        t1.start(); t2.start(); t3.start();

        boolean finished = latch.await(2, TimeUnit.SECONDS);
        assertTrue(finished, "All threads should have finished within timeout");
    }
}