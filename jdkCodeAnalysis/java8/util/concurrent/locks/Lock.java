package java.util.concurrent.locks;
import java.util.concurrent.TimeUnit;

/**
 * 互斥锁（mutex）的本质就是一个内存标志，这个标志可以是一个flag（占用标志），
 * 也可以是一个指针，指向一个持有者的线程ID，也可以是两个都有，
 * 以及一个等待（阻塞）队列，以及若干其它信息等。
 * 当这个flag被标记成被占用的时候，或者持有者指针不为空的时候，那么它就不能被被别的任务（线程）访问。
 * 只有等到这个mutex变得空闲的时候，操作系统会把等待队列里的第一任务（线程）取出来，然后调度执行，
 * 如果当前CPU很忙，那么就把取出的这个任务（线程）标记为就绪（READY）状态，后续如果CPU空闲了，就会被调度。
 */
public interface Lock {

    void lock();

    void lockInterruptibly() throws InterruptedException;

    boolean tryLock();

    boolean tryLock(long time, TimeUnit unit) throws InterruptedException;

    void unlock();

    Condition newCondition();
}
