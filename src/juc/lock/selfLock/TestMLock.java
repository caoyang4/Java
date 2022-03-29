package src.juc.lock.selfLock;

import java.util.concurrent.locks.Lock;

/**
 * @author caoyang
 */
public class TestMLock {
    public static long sum = 0;
    public static void main(String[] args) throws InterruptedException {
        final int size = 100;
        Lock lock = new MLock();
        Thread[] threads = new Thread[size];
        for (int i = 0; i < size; i++) {
            threads[i] = new Thread(() -> {
                lock.lock();
                try {
                    for (int j = 0; j < 10000; j++) {
                        sum++;
                    }
                } finally {
                    lock.unlock();
                }
            });
        }
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        System.out.println("sum=" + sum);
    }
}
