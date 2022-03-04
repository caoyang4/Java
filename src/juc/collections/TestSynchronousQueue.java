package src.juc.collections;

import java.util.Random;
import java.util.concurrent.*;

/**
 * 同步队列
 * 内部同时只能够容纳单个元素，相当于一个汇合点
 * @author caoyang
 */
public class TestSynchronousQueue {
    public static void main(String[] args) {
        BlockingQueue<String> queue = new SynchronousQueue<>();
        Thread producer = new Thread(() -> {
            String event = "BlockingQueue";
            try {
                for (int i = 0; i < 10; i++) {
                    // thread will block here
                    queue.put(event+i);
                    System.out.printf("[%s] published event : %s \n", Thread.currentThread().getName(),event+i);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "Producer");

        producer.start(); // starting publisher thread

        Thread consumer = new Thread(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    String event = queue.take();
                    System.out.printf("[%s] consumed event : %s \n", Thread.currentThread().getName(), event);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "Consumer");

        consumer.start(); // starting consumer thread


    }
}
