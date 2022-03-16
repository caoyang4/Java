package src.juc.lock;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * @author caoyang
 */
public class TestCyclicBarrier2 {
    public static void main(String[] args) throws BrokenBarrierException, InterruptedException {
        CyclicBarrier barrier = new CyclicBarrier(3, () -> {
            System.out.println(Thread.currentThread().getName() + " break barrier...");
        });
        for (int i = 1; i <= 2; i++) {
            new Thread(() -> {
                System.out.println(Thread.currentThread().getName() + " going to await");
                try {
                    barrier.await();
                    System.out.println(Thread.currentThread().getName() + " continue");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }, "thread"+i).start();
        }
        // 主线程等待
        System.out.println(Thread.currentThread().getName() + " going to await");
        barrier.await();
        System.out.println(Thread.currentThread().getName() + " continue");
    }
}
