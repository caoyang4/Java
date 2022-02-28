package src.juc.lock;

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

        for (int i=1; i<=threads.length; i++) {
            threads[i-1] = new Thread(() -> {
                out.println(Thread.currentThread().getName() +" go!");
                latch.countDown();
                out.println(latch.getCount());
            }, "Thread"+i);
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
