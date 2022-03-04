package src.juc.atomic;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * LongAdder
 * 用1000个并发任务，每个任务对数据累加10000次，每个实验测试10次。
 * @author caoyang
 */
public class TestLongAdder {
    private static final int TASK_NUM = 1000;
    private static final int INCREMENT_PER_TASK = 100000;
    private static final int REPEAT = 10;

    private static long l = 0;

    public static void main(String[] args) throws Exception {
        repeatWithStatics("[LongAdder]", REPEAT, () -> testLongAdder());
        repeatWithStatics("[Long]", REPEAT, () -> testLong());
        repeatWithStatics("[AtomicLong]", REPEAT, () -> testAtomicLong());
    }

    public static void testAtomicLong() {
        AtomicLong atomicLong = new AtomicLong(0);
        execute(TASK_NUM, () -> repeat(INCREMENT_PER_TASK, () -> atomicLong.incrementAndGet()));
    }

    public static void testLong() {
        l = 0;
        execute(TASK_NUM, () -> repeat(INCREMENT_PER_TASK, () -> l++));
    }

    public static void testLongAdder() {
        LongAdder longAdder = new LongAdder();
        execute(TASK_NUM, () -> repeat(INCREMENT_PER_TASK, () -> longAdder.add(1)));
    }

    public static void repeatWithStatics(String type, int n, Runnable runnable) {
        long[] elapses = new long[n];
        nTimes(n).forEach(x -> {
            long start = System.currentTimeMillis();
            runnable.run();
            long end = System.currentTimeMillis();
            elapses[x] = end - start;
        });
        System.out.printf(type+" total: %d, %s\n", Arrays.stream(elapses).sum(), Arrays.toString(elapses));
    }

    private static void execute(int n, Runnable task) {
        try {
            CountDownLatch latch = new CountDownLatch(n);
            ExecutorService service = Executors.newFixedThreadPool(100);
            Runnable taskWrapper = () -> {
                task.run();
                latch.countDown();
            };
            service.invokeAll(cloneTask(n, taskWrapper));
            latch.await();
            service.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Collection<Callable<Void>> cloneTask(int n, Runnable task) {
        return nTimes(n).mapToObj(x ->
                (Callable<Void>) () -> {
                    task.run();
                    return null;
                }).collect(Collectors.toList());
    }

    private static void repeat(int n, Runnable runnable) {
        nTimes(n).forEach(x -> {
            runnable.run();
        });
    }

    private static IntStream nTimes(int n) {
        return IntStream.range(0, n);
    }

}
