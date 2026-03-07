package com.vbforge.threads.basic;

/**
 * Demonstrates the 3 classic ways to create threads in Java 17.
 * Run with: mvn exec:java -Dexec.mainClass="com.vbforge.threads.basic.ThreadCreationDemo"
 * (add maven-exec-plugin if you want the exec goal).
 */
public class ThreadCreationDemo {

    // 1. Extending Thread
    static class MyThread extends Thread {
        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName() + " (extends Thread)");
        }
    }

    // 2. Implementing Runnable
    static class MyRunnable implements Runnable {
        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName() + " (implements Runnable)");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Main thread: " + Thread.currentThread().getName());

        Thread t1 = new MyThread();
        Thread t2 = new Thread(new MyRunnable());
        Thread t3 = new Thread(() -> System.out.println(Thread.currentThread().getName() + " (lambda)"));

        t1.start();
        t2.start();
        t3.start();

        t1.join();
        t2.join();
        t3.join();

        System.out.println("All threads finished.");
    }
}