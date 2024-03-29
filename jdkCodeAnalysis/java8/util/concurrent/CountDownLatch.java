
package java.util.concurrent;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * class Driver {
 *   void main() throws InterruptedException {
 *     CountDownLatch startSignal = new CountDownLatch(1);
 *     CountDownLatch doneSignal = new CountDownLatch(N);
 *
 *     for (int i = 0; i < N; ++i) // create and start threads
 *       new Thread(new Worker(startSignal, doneSignal)).start();
 *
 *       doSomethingElse();            // don't let run yet
 *       startSignal.countDown();      // let all threads proceed
 *       doSomethingElse();
 *       doneSignal.await();           // wait for all to finish
 *   }
 * }
 *
 * class Worker implements Runnable {
 *   private final CountDownLatch startSignal;
 *   private final CountDownLatch doneSignal;
 *   Worker(CountDownLatch startSignal, CountDownLatch doneSignal) {
 *     this.startSignal = startSignal;
 *     this.doneSignal = doneSignal;
 *   }
 *   public void run() {
 *     try {
 *       startSignal.await();
 *       doWork();
 *       doneSignal.countDown();
 *     } catch (InterruptedException ex) {} // return;
 *   }
 *
 *   void doWork() { ... }
 * }}

 * class Driver2 {
 *   void main() throws InterruptedException {
 *     CountDownLatch doneSignal = new CountDownLatch(N);
 *     Executor e = ...
 *
 *     for (int i = 0; i < N; ++i) // create and start threads
 *       e.execute(new WorkerRunnable(doneSignal, i));
 *
 *     doneSignal.await();           // wait for all to finish
 *   }
 * }
 *
 * class WorkerRunnable implements Runnable {
 *   private final CountDownLatch doneSignal;
 *   private final int i;
 *   WorkerRunnable(CountDownLatch doneSignal, int i) {
 *     this.doneSignal = doneSignal;
 *     this.i = i;
 *   }
 *   public void run() {
 *     try {
 *       doWork(i);
 *       doneSignal.countDown();
 *     } catch (InterruptedException ex) {} // return;
 *   }
 *
 *   void doWork() { ... }
 * }}
 */

/**
 * CountDownlatch 基于 AQS 实现，会将构造 CountDownLatch 的入参传递至 state，countDown() 就是在利用 CAS 将 state 减 - 1，
 * await() 实际就是让头节点一直在等待 state 为 0 时，释放所有等待的线程
 *
 * CountDownLatch 是 Java 并发包中的一个工具类，它可以通过一个计数器实现多个线程之间的相互等待，直到计数器的值达到一个预期值，所有等待线程才会继续执行。
 *
 * CountDownLatch 的应用场景如下：
 * 1. 主线程等待多个子线程执行结束后再执行；
 * 2. 同时开始多个线程执行任务，等到它们全部执行结束后再执行下一步工作；
 * 3. 等待多个分布式服务都启动后再执行主业务流程。
 *
 * 需要注意的是，CountDownLatch 的计数器一旦被计数之后就无法重置，
 * 因此在使用时需要注意控制计数器的值，避免出现计数错误等问题。
 * 同时，由于 CountDownLatch 的计数器只能减少，因此可能会出现线程一直阻塞的情况，
 * 需要根据实际场景配合使用超时等控制策略。
 */
public class CountDownLatch {

    private static final class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = 4982264981922014374L;

        // 任务数就是AQS的state的初始值
        Sync(int count) {
            setState(count);
        }

        int getCount() {
            return getState();
        }

        protected int tryAcquireShared(int acquires) {
            return (getState() == 0) ? 1 : -1;
        }

        protected boolean tryReleaseShared(int releases) {
            // Decrement count; signal when transition to zero
            for (;;) {
                int c = getState();
                if (c == 0)
                    return false;
                int nextc = c-1;
                if (compareAndSetState(c, nextc))
                    // 只有一种情况会返回true，那就是state值从大于0变为0值时
                    return nextc == 0;
            }
        }
    }

    private final Sync sync;

    public CountDownLatch(int count) {
        if (count < 0) throw new IllegalArgumentException("count < 0");
        this.sync = new Sync(count);
    }

    public void await() throws InterruptedException {
        // 底层实现是中断式获取共享锁，阻塞式地等待，并且是响应中断
        // 它不是在等待signal操作，而是在等待count值为0
        sync.acquireSharedInterruptibly(1);
    }

    public boolean await(long timeout, TimeUnit unit)
        throws InterruptedException {
        return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
    }

    public void countDown() {
        sync.releaseShared(1);
    }

    public long getCount() {
        return sync.getCount();
    }

    public String toString() {
        return super.toString() + "[Count = " + sync.getCount() + "]";
    }
}
