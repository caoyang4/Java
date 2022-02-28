package src.juc.lock;

import java.util.concurrent.Semaphore;

/**
 * @author caoyang
 */
public class TestSemaphore {
    public static void main(String[] args) {
        // permits 允许信号量
        // 限流
        Semaphore semaphore = new Semaphore(2);

        new Thread(() -> {
            try {
                System.out.println("T1 running...");
                // 阻塞，信号量 -1
                semaphore.acquire();
                System.out.println("T1 running!!!");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                semaphore.release();
            }
        }).start();

        new Thread(( )-> {
            try {
                System.out.println("T2 running...");
                semaphore.acquire();
                System.out.println("T2 running!!!");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                semaphore.release();
            }
        }).start();
    }
}
