package src.juc.lock;

import java.util.concurrent.CountDownLatch;

import static java.lang.System.*;

/**
 * CountDownLatch
 * 此处与 join 类似
 * @author caoyang
 */
public class TestCountDownLatch1 {
    public static void main(String[] args) {
        Thread[] threads = new Thread[100];
        CountDownLatch latch = new CountDownLatch(5);

        for (int i=1; i<=threads.length; i++) {
            threads[i-1] = new Thread(() -> {
                try {
                    out.println(Thread.currentThread().getName() +" start!");
                    Thread.sleep(1000);
                    out.println(Thread.currentThread().getName() +" end!");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                latch.countDown();
            }, "Thread"+i);
        }
        out.println("start");
        for (Thread thread : threads) {
            thread.start();
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        out.println("end");
    }
}
