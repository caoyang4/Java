package src.juc.cacheline;

import sun.misc.Contended;

/**
 * 伪共享
 *
 * 针对处在同一个缓存行内的数据，假设线程1修改了其中的一个数据a后，线程2想要读取数据a，
 * 因为a已经被修改了，因此缓存行失效，需要从主内存中重新读取。
 * 这种无法充分使用缓存行特性的现象，称为伪共享。
 * 当多线程修改互相独立的变量时，如果这些变量共享同一个缓存行，就会无意中影响彼此的性能，这就是伪共享。
 */
public class FalseSharing implements Runnable{
    public final static long ITERATIONS = 500L * 1000L * 100L;
    private int arrayIndex = 0;

    private static ValueNoPadding[] longsNoPadding;

    private static ValuePadding[] longsPadding;

    private boolean padding;

    public FalseSharing(final int arrayIndex, boolean padding) {
        this.arrayIndex = arrayIndex;
        this.padding = padding;
    }

    public static void main(final String[] args) throws Exception {
        for(int i=1; i<10; i++){
            // 先执行 gc
            System.gc();
            final long start = System.currentTimeMillis();
            runNoPadding(i);
            System.out.println("NoPadding Thread"+i+" cost " + (System.currentTimeMillis() - start) + " ms");
        }

        for(int i=1; i<10; i++){
            // 先执行 gc
            System.gc();
            final long start = System.currentTimeMillis();
            runPadding(i);
            System.out.println("Padding Thread"+i+" cost " + (System.currentTimeMillis() - start) + " ms");
        }

    }

    private static void runPadding(int NUM_THREADS) throws InterruptedException {
        Thread[] threads = new Thread[NUM_THREADS];
        longsPadding = new ValuePadding[NUM_THREADS];
        for (int i = 0; i < longsPadding.length; i++) {
            longsPadding[i] = new ValuePadding();
        }
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new FalseSharing(i, true));
        }

        for (Thread t : threads) {
            t.start();
        }

        for (Thread t : threads) {
            t.join();
        }
    }

    private static void runNoPadding(int NUM_THREADS) throws InterruptedException {
        Thread[] threads = new Thread[NUM_THREADS];
        longsNoPadding = new ValueNoPadding[NUM_THREADS];
        for (int i = 0; i < longsNoPadding.length; i++) {
            longsNoPadding[i] = new ValueNoPadding();
        }
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new FalseSharing(i, false));
        }

        for (Thread t : threads) {
            t.start();
        }

        for (Thread t : threads) {
            t.join();
        }
    }

    public void run() {
        long i = ITERATIONS + 1;
        while (0 != --i) {
            if (padding) {
                longsPadding[arrayIndex].value = 0L;
            } else {
                longsNoPadding[arrayIndex].value = 0L;
            }
        }
    }

    // 使用缓存行对齐
    public final static class ValuePadding {
        protected long p1, p2, p3, p4, p5, p6, p7;
        protected volatile long value = 0L;
        protected long p9, p10, p11, p12, p13, p14, p15;
    }
   // 不使用缓存行对齐
    public final static class ValueNoPadding {
        protected volatile long value = 0L;
    }
}
