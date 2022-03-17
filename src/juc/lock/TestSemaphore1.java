package src.juc.lock;

import java.util.concurrent.Semaphore;

/**
 * semaphore初始化有10个令牌，一个线程重复调用11次acquire方法，会发生什么?
 * 线程阻塞，不会继续往下运行，令牌没有重入的概念
 * release方法会添加令牌，并不会以初始化的大小为准
 * @author caoyang
 */
public class TestSemaphore1 {
    public static void main(String[] args) {
        // permits 允许信号量
        // 限流
        Semaphore semaphore = new Semaphore(2);

        new Thread(() -> {
            try {
                System.out.println("T1 start running...");
                // 阻塞，信号量 -1
                semaphore.acquire(2);
                System.out.println("T1 end running!!!");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println("availablePermits: "+semaphore.availablePermits());
                semaphore.release();
            }
        }).start();

        new Thread(( )-> {
            try {
                System.out.println("T2 start running...");
                semaphore.acquire();
                System.out.println("T2 end running!!!");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                semaphore.release();
            }
        }).start();
    }
}
