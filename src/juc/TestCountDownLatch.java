package juc;

import java.util.concurrent.CountDownLatch;

import static java.lang.System.*;

/**
 * CountDownLatch
 * 此处与 join 类似
 * @author caoyang
 */
public class TestCountDownLatch {
    public static void main(String[] args) {
        Thread[] threads = new Thread[100];
        CountDownLatch latch = new CountDownLatch(5);

        for (int i=0; i<threads.length; i++) {
            threads[i] = new Thread(() -> {
                out.println("go!");
                latch.countDown();
                out.println(latch.getCount());
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }
        try {
            out.println("start");
            latch.await();
            out.println("end");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
