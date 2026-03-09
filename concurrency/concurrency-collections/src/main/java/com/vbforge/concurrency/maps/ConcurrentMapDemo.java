package com.vbforge.concurrency.maps;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Demonstrates why ConcurrentHashMap is usually preferred over synchronized HashMap
 */
public class ConcurrentMapDemo {

    public static void main(String[] args) throws InterruptedException {
        var map = new ConcurrentHashMap<String, Integer>();

        Runnable task = () -> {
            for (int i = 0; i < 10000; i++) {
                String key = "key-" + (i % 50);
                map.compute(key, (k, v) -> (v == null) ? 1 : v + 1);
            }
        };

        ExecutorService executor = Executors.newFixedThreadPool(8);
        for (int i = 0; i < 8; i++) {
            executor.submit(task);
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        System.out.println("Final size = " + map.size());
        long total = map.values().stream().mapToLong(Integer::longValue).sum();
        System.out.println("Total increments = " + total);  // will be 8 * 10000 / 50 * 50 = 80000
    }

}
