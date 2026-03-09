package com.vbforge.concurrency.lists;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CopyOnWriteListDemo {

    public static void main(String[] args) throws InterruptedException {
        List<String> list = new CopyOnWriteArrayList<>();

        Runnable reader = () -> {
            for (int i = 0; i < 100; i++) {
                // Safe iteration even while others write
                for (String s : list) {
                    // simulate work
                }
            }
        };

        Runnable writer = () -> {
            for (int i = 0; i < 300; i++) {
                list.add("item-" + i);
            }
        };

        ExecutorService executor = Executors.newFixedThreadPool(5);

        executor.submit(writer);
        for (int i = 0; i < 4; i++) {
            executor.submit(reader);
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        System.out.println("Final size: " + list.size());
    }
}