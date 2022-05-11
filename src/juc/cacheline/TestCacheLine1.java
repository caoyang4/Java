package src.juc.cacheline;

import sun.misc.Contended;

import java.util.concurrent.CountDownLatch;

/**
 * 缓存行对齐测试
 * @author caoyang
 */
public class TestCacheLine1 {
    public static final long COUNT = 100000000L;
    private static class Line{
        private long p1, p2, p3, p4, p5, p6, p7;
        public long x = 0L;
        private long p9, p10, p11, p12, p13, p14, p15;
    }
    public static volatile Line[] arr = new Line[2];

    static {
        arr[0] = new Line();
        arr[1] = new Line();
    }
    public static void main(String[] args) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < COUNT; i++) {
                arr[0].x = i;
            }
            latch.countDown();
        });
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < COUNT; i++) {
                arr[1].x = i;
            }
            latch.countDown();
        });
        final long start = System.nanoTime();
        t1.start();
        t2.start();
        latch.await();
        final long end = System.nanoTime();
        System.out.println("cost: "+((end - start)/1000000L) + "ms");
    }
}
