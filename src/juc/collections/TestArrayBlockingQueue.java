package src.juc.collections;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.LockSupport;

/**
 * 双端队列，两端都可以插入、提取数据
 * @author caoyang
 */
public class TestArrayBlockingQueue {
    public static void main(String[] args) {
        BlockingQueue<Integer> queue = new ArrayBlockingQueue(32);
        Thread producer = new Thread(
                () -> {
                    try {
                        for (int i = 0; ; i++) {
                            queue.put(i);
                            System.out.println("producer put " + i);
                            Thread.sleep(1000);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
        );

        Thread consumer1 = new Thread(
                () -> {
                    try {
                        while (true){
                            System.out.println("consumer1 get " + queue.take());
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
        );

        Thread consumer2 = new Thread(
                () -> {
                    try {
                        while (true){
                            // 队列为空就阻塞等待，直到有，继续消费
                            System.out.println("consumer2 get " + queue.take());
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
        );

        producer.start();
        consumer1.start();
        consumer2.start();
    }
}

