/**
 * CyclicBarrier 则利用 ReentrantLock 和 Condition，自身维护了 count 和 parties 变量。
 * 每次调用 await 将 count-1，并将线程加入到 condition 队列上。
 * 等到 count 为 0 时，则将 condition 队列的节点移交至 AQS 队列，并全部释放。
 *
 * CyclicBarrier 的应用场景如下：
 * 1. 一组线程需要等待彼此到达某个节点再进行下一步操作，例如商城搭建时，需要等前置条件完成后才能启动服务器；
 * 2. 一项任务需要分解成多个子任务进行处理，每个子任务的结果相互依赖，只有当所有子任务完成后才能进行下一步操作。
 *
 * 需要注意的是，CyclicBarrier 的计数器可以重置，因此可以被多次使用。
 * 同时，由于 CyclicBarrier 可以阻塞线程，因此可能会出现线程一直等待的情况，需要根据实际场景使用超时等控制策略。
 *
 */

package java.util.concurrent;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

// CyclicBarrier实现了类似CountDownLatch的逻辑，它可以使得一组线程之间相互等待，直到所有的线程都到齐了之后再继续往下执行。
// CyclicBarrier基于条件队列和独占锁来实现，而非共享锁。
// CyclicBarrier可重复使用，在所有线程都到齐了一起通过后，将会开启新的一代。
// CyclicBarrier使用了“all-or-none breakage model”，所有互相等待的线程，要么一起通过barrier，
// 要么一个都不要通过，如果有一个线程因为中断，失败或者超时而过早的离开了barrier，则该barrier会被broken掉，
// 所有等待在该barrier上的线程都会抛出BrokenBarrierException（或者InterruptedException）


// CountDownLatch是一次性的，当count值被减为0后，不会被重置;
// 而CyclicBarrier在线程通过栅栏后，会开启新的一代，count值会被重置

// CyclicBarrier使用的是独占锁，count值不为0时，线程进入condition queue中等待，
// 当count值降为0后，将被signalAll()方法唤醒到sync queue中去，
// 然后挨个去争锁（因为是独占锁），在前驱节点释放锁以后，才能继续唤醒后继节点。
public class CyclicBarrier {

    private static class Generation {
        boolean broken = false;
    }

    // CyclicBarrier是基于独占锁ReentrantLock和条件队列实现的，而不是共享锁，
    // 所有相互等待的线程都会在同样的条件队列trip上挂起，
    // 被唤醒后将会被添加到sync queue中去争取独占锁lock，获得锁的线程将继续往下执行
    private final ReentrantLock lock = new ReentrantLock();

    private final Condition trip = lock.newCondition();

    private final int parties;

    private final Runnable barrierCommand;

    private Generation generation = new Generation();

    // 代表还需要等待的线程数，初始值为parties，每当一个线程到来就减一，如果该值为0，则说明所有的线程都到齐了，大家可以一起通过barrier了
    private int count;

    // 栏杆每打开关闭一次，就产生新一的“代”
    private void nextGeneration() {
        // // 唤醒当前generation中所有等待在条件队列里的线程
        trip.signalAll();
        // 恢复count值，开启新的一代
        count = parties;
        generation = new Generation();
    }

    // breakBarrier即打破现有的栅栏，让所有线程通过
    private void breakBarrier() {
        // 标记broken状态
        generation.broken = true;
        // 恢复count值
        count = parties;
        // 唤醒当前这一代中所有等待在条件队列里的线程（因为栅栏已经打破了）
        trip.signalAll();
    }

    private int dowait(boolean timed, long nanos) throws InterruptedException, BrokenBarrierException, TimeoutException {
        final ReentrantLock lock = this.lock;
        // 所有执行await方法的线程必须是已经持有了锁，所以这里必须先获取锁
        lock.lock();
        try {
            final Generation g = generation;
            // 如果一个正在await的线程发现barrier已经被break了，则将直接抛出BrokenBarrierException异常
            if (g.broken)
                throw new BrokenBarrierException();
            // 如果当前线程被中断了，则先将栅栏打破，再抛出InterruptedException
            // 这么做的原因是，所以等待在barrier的线程都是相互等待的，如果其中一个被中断了，那其他的就不用等了。
            if (Thread.interrupted()) {
                breakBarrier();
                throw new InterruptedException();
            }
            // 当前线程已经来到了栅栏前，先将等待的线程数减一
            int index = --count;
            if (index == 0) {
                // 如果等待的线程数为0了，说明所有的parties都到齐了
                // 则可以唤醒所有等待的线程，让大家一起通过栅栏，并重置栅栏
                boolean ranAction = false;
                try {
                    final Runnable command = barrierCommand;
                    if (command != null)
                        // 如果创建CyclicBarrier时传入了barrierCommand
                        // 说明通过栅栏前有一些额外的工作要做
                        command.run();
                    ranAction = true;
                    // 唤醒所有线程，开启新一代
                    nextGeneration();
                    return 0;
                } finally {
                    if (!ranAction)
                        breakBarrier();
                }
            }

            // 如果count数不为0，就将当前线程挂起，直到所有的线程到齐，或者超时，或者中断发生
            for (;;) {
                try {
                    if (!timed)
                        // 如果没有设定超时机制，则直接调用condition的await方法
                        trip.await();
                    else if (nanos > 0L)
                        // 当前线程在这里被挂起，超时时间到了就会自动唤醒
                        nanos = trip.awaitNanos(nanos);
                } catch (InterruptedException ie) {
                    if (g == generation && ! g.broken) {
                        // 执行到这里说明线程被中断了
                        // 如果线程被中断时还处于当前这一“代”，并且当前这一代还没有被broken,则先打破栅栏
                        breakBarrier();
                        throw ie;
                    } else {
                        // 注意来到这里有两种情况
                        // 一种是g!=generation，说明新的一代已经产生了，所以我们没有必要处理这个中断，只要再自我中断一下就好，交给后续的人处理
                        // 一种是g.broken = true, 说明中断前栅栏已经被打破了，既然中断发生时栅栏已经被打破了，也没有必要再处理这个中断了
                        Thread.currentThread().interrupt();
                    }
                }

                if (g.broken)
                    throw new BrokenBarrierException();

                if (g != generation)
                    return index;

                if (timed && nanos <= 0L) {
                    breakBarrier();
                    throw new TimeoutException();
                }
            }
        } finally {
            lock.unlock();
        }
    }

    // Runnable barrierAction 类似于一个钩子方法
    public CyclicBarrier(int parties, Runnable barrierAction) {
        if (parties <= 0) throw new IllegalArgumentException();
        this.parties = parties;
        this.count = parties;
        this.barrierCommand = barrierAction;
    }


    public CyclicBarrier(int parties) {
        this(parties, null);
    }


    public int getParties() {
        return parties;
    }


    public int await() throws InterruptedException, BrokenBarrierException {
        try {
            return dowait(false, 0L);
        } catch (TimeoutException toe) {
            throw new Error(toe); // cannot happen
        }
    }


    public int await(long timeout, TimeUnit unit)
        throws InterruptedException,
               BrokenBarrierException,
               TimeoutException {
        return dowait(true, unit.toNanos(timeout));
    }

    public boolean isBroken() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return generation.broken;
        } finally {
            lock.unlock();
        }
    }

    // reset方法用于将barrier恢复成初始的状态，它的内部就是简单地调用了breakBarrier方法和nextGeneration方法。
    public void reset() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            breakBarrier();   // break the current generation
            nextGeneration(); // start a new generation
        } finally {
            lock.unlock();
        }
    }
    // 还缺多少线程可以发车
    public int getNumberWaiting() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return parties - count;
        } finally {
            lock.unlock();
        }
    }
}
