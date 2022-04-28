package java.util.concurrent;
import java.util.concurrent.locks.LockSupport;

/**
 * FutureTask包含了Future和Task两部分
 * FutureTask实现了RunnableFuture接口，即Runnable接口和Future接口。
 * 其中Runnable接口对应了FutureTask名字中的Task，代表FutureTask本质上也是表征了一个任务。
 * 而Future接口就对应了FutureTask名字中的Future，表示了我们对于这个任务可以执行某些操作，
 * 例如，判断任务是否执行完毕，获取任务的执行结果，取消任务的执行等
 * @param <V>
 */
public class FutureTask<V> implements RunnableFuture<V> {

    // volatile确保了不同线程对它修改的可见性
    // 初始态：NEW
    // 中间态：COMPLETING、INTERRUPTING 任务的中间状态是一个瞬态，非常的短暂
    //        任务的中间态并不代表任务正在执行，而是任务已经执行完了，正在设置最终的返回结果
    // 终止态：NORMAL、EXCEPTIONAL、CANCELLED、INTERRUPTED
    // 将一个任务的状态设置成终止态只有三种方法：
    //    set
    //    setException
    //    cancel
    private volatile int state;
    private static final int NEW          = 0;
    private static final int COMPLETING   = 1; // 正在设置任务结果，意味着call方法已经执行完毕，正在设置任务执行的结果
    private static final int NORMAL       = 2; // 任务正常执行完毕
    private static final int EXCEPTIONAL  = 3; // 任务执行过程中发生异常
    private static final int CANCELLED    = 4; // 任务被取消
    private static final int INTERRUPTING = 5; // 正在中断运行任务的线程
    private static final int INTERRUPTED  = 6; // 任务被中断

    /**
     * 任务本尊：callable
     * 任务的执行者：runner
     * 任务的结果：outcome
     * 获取任务的结果：state + outcome + waiters
     * 中断或者取消任务：state + runner + waiters
     */

    private Callable<V> callable; // 任务本尊
    // outcome 非volatile，因为state读写保护
    private Object outcome; // non-volatile, protected by state reads/writes
    // 执行FutureTask中的“Task”的线程
    // 为什么需要一个属性来记录执行任务的线程呢？这是为了中断或者取消任务做准备的，只有知道了执行任务的线程是谁，我们才能去中断它
    private volatile Thread runner;
    // 任务队列头，单向链表的头节点
    private volatile WaitNode waiters;

    /**
     * 根据当前state状态，返回正常执行的结果，或者抛出指定的异常
     */
    @SuppressWarnings("unchecked")
    private V report(int s) throws ExecutionException {
        Object x = outcome;
        if (s == NORMAL)
            return (V)x;
        if (s >= CANCELLED)
            throw new CancellationException();
        throw new ExecutionException((Throwable)x);
    }

    public FutureTask(Callable<V> callable) {
        if (callable == null) throw new NullPointerException();
        this.callable = callable;
        // state在callable后面初始化，保证callable的可见性
        this.state = NEW;       // ensure visibility of callable
    }

    public FutureTask(Runnable runnable, V result) {
        this.callable = Executors.callable(runnable, result);
        this.state = NEW;       // ensure visibility of callable
    }

    // CANCELLED、INTERRUPTING、INTERRUPTED都认定为任务已取消
    public boolean isCancelled() {
        return state >= CANCELLED;
    }

    // state只要不是NEW状态，任务就是执行结束
    public boolean isDone() {
        return state != NEW;
    }

    /**
     * 首先有以下三种情况之一的，cancel操作一定是失败的：
     *   任务已经执行完成了
     *   任务已经被取消过了
     *   任务因为某种原因不能被取消
     * 值得注意的是，cancel操作返回true并不代表任务真的就是被取消了，这取决于发动cancel状态时，任务所处的状态
     * cancel方法实际上完成以下两种状态转换之一:
     *   NEW -> CANCELLED (对应于mayInterruptIfRunning=false)
     *   NEW -> INTERRUPTING -> INTERRUPTED (对应于mayInterruptIfRunning=true)
     */
    public boolean cancel(boolean mayInterruptIfRunning) {
        // 只要state不为NEW，表明任务已经执行完成，则直接返回false
        if (!(state == NEW &&
              // 根据mayInterruptIfRunning的值将state的状态由NEW设置成INTERRUPTING或者CANCELLED
              UNSAFE.compareAndSwapInt(this, stateOffset, NEW, mayInterruptIfRunning ? INTERRUPTING : CANCELLED)))
            return false;
        // 说明 state 是NEW，或者状态cas修改为INTERRUPTING或CANCELLED
        try {
            // 如果mayInterruptIfRunning 为true, 则当前在执行的任务会被中断
            // 如果mayInterruptIfRunning 为false, 则可以允许正在执行的任务继续运行，直到它执行完
            if (mayInterruptIfRunning) {
                try {
                    Thread t = runner;
                    if (t != null)
                        t.interrupt();
                } finally { // final state
                    UNSAFE.putOrderedInt(this, stateOffset, INTERRUPTED);
                }
            }
        } finally {
            finishCompletion();
        }
        return true;
    }

    /**
     * FutureTask中会涉及到两类线程：
     *  一类是执行任务的线程，它只有一个，FutureTask的run方法就由该线程来执行；
     *  另一类是获取任务执行结果的线程，它可以有多个，这些线程可以并发执行，每一个线程都是独立的，都可以调用get方法来获取任务的执行结果。
     *  如果任务还没有执行完，则这些线程就需要进入Treiber栈中挂起，直到任务执行结束，或者等待的线程自身被中断
     */
    public V get() throws InterruptedException, ExecutionException {
        int s = state;
        // 当任务还没有执行完毕或者正在设置执行结果时，我们就使用awaitDone方法等待任务进入终止态
        if (s <= COMPLETING)
            s = awaitDone(false, 0L);
        return report(s);
    }

    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (unit == null)
            throw new NullPointerException();
        int s = state;
        if (s <= COMPLETING &&
            (s = awaitDone(true, unit.toNanos(timeout))) <= COMPLETING)
            throw new TimeoutException();
        return report(s);
    }

    /**
     * 这个方法是一个空方法，它是提供给子类覆写的，以实现一些任务执行结束前的额外操作。
     */
    protected void done() { }

    protected void set(V v) {
        if (UNSAFE.compareAndSwapInt(this, stateOffset, NEW, COMPLETING)) {
            outcome = v;
            // putOrderedInt把状态刷到主内存保证其他线程都可见，由于happens-before原则，outcome对其他线程也可见
            // 由于state属性被设置成volatile，putOrderedInt应当和putIntVolatile是等价的，保证了state状态对其他线程的可见性
            UNSAFE.putOrderedInt(this, stateOffset, NORMAL); // final state
            // 唤醒其他任务
            finishCompletion();
        }
    }

    protected void setException(Throwable t) {
        // outcome属性赋值为异常对象，以及将state的终止状态修改为EXCEPTIONAL
        if (UNSAFE.compareAndSwapInt(this, stateOffset, NEW, COMPLETING)) {
            outcome = t;
            UNSAFE.putOrderedInt(this, stateOffset, EXCEPTIONAL); // final state
            finishCompletion();
        }
    }

    /**
     * run方法重点做了以下几件事：
     *  1、将runner属性设置成当前正在执行run方法的线程
     *  2、调用callable成员变量的call方法来执行任务
     *  3、设置执行结果outcome, 如果执行成功, 则outcome保存的就是执行结果；如果执行过程中发生了异常, 则outcome中保存的就是异常，设置结果之前，先将state状态设为中间态
     *  4、对outcome的赋值完成后，设置state状态为终止态(NORMAL或者EXCEPTIONAL)
     *  5、唤醒等待队列中所有等待的线程
     *  6、善后清理(waiters, callable，runner设为null)
     *  7、检查是否有遗漏的中断，如果有，等待中断状态完成。
     */
    public void run() {
        // 检查当前状态是不是New
        // 所谓的“state只要不是NEW状态，就说明任务已经执行完成了”就体现在这里，
        // 因为run方法中，我们是在call()执行完毕或者抛出了异常之后才开始设置中间态和终止态的
        if (state != NEW ||
            // CAS操作将runner属性设置位当前线程，即记录执行任务的线程
            // 此处可见，runner属性是在执行run方法时被初始化
            !UNSAFE.compareAndSwapObject(this, runnerOffset, null, Thread.currentThread()))
            return;
        try {
            Callable<V> c = callable;
            if (c != null && state == NEW) {
                V result;
                boolean ran;
                try {
                    result = c.call();
                    ran = true;
                } catch (Throwable ex) {
                    result = null;
                    ran = false;
                    // 任务失败，setException(ex)设置抛出异常
                    setException(ex);
                }
                // 任务执行成功，set(result)设置结果
                if (ran)
                    set(result);
            }
        } finally {
            // runner must be non-null until state is settled to
            // prevent concurrent calls to run()
            runner = null;
            // state must be re-read after nulling runner to prevent
            // leaked interrupts
            int s = state;
            // 检查有没有遗漏的中断
            if (s >= INTERRUPTING)
                // 之前已经执行过的set方法或者setException方法不是已经将state状态设置成NORMAL或者EXCEPTIONAL了吗？
                // 怎么会出现INTERRUPTING或者INTERRUPTED状态呢
                // 在多线程的环境中，在当前线程执行run方法的同时，有可能其他线程取消了任务的执行，
                // 此时其他线程就可能对state状态进行改写，这也就是我们在设置终止状态的时候用putOrderedInt方法，如果用cas方式，就可能set失败
                // 而没有用CAS操作的原因——我们无法确信在设置state前是处于COMPLETING中间态还是INTERRUPTING中间态
                // 响应cancel方法所造成的中断最大的意义不是为了对中断进行处理，而是简单的停止任务线程的执行，节省CPU资源
                handlePossibleCancellationInterrupt(s);
        }
    }

    protected boolean runAndReset() {
        if (state != NEW ||
            !UNSAFE.compareAndSwapObject(this, runnerOffset, null, Thread.currentThread()))
            return false;
        boolean ran = false;
        int s = state;
        try {
            Callable<V> c = callable;
            if (c != null && s == NEW) {
                try {
                    c.call(); // don't set result
                    ran = true;
                } catch (Throwable ex) {
                    setException(ex);
                }
            }
        } finally {
            // runner must be non-null until state is settled to
            // prevent concurrent calls to run()
            runner = null;
            // state must be re-read after nulling runner to prevent
            // leaked interrupts
            s = state;
            if (s >= INTERRUPTING)
                handlePossibleCancellationInterrupt(s);
        }
        return ran && s == NEW;
    }

    private void handlePossibleCancellationInterrupt(int s) {
        if (s == INTERRUPTING)
            // 该方法是一个自旋操作，如果当前的state状态是INTERRUPTING，我们在原地自旋，直到state状态转换成终止态
            while (state == INTERRUPTING)
                Thread.yield(); // wait out pending interrupt
    }

    /**
     * 在FutureTask中，队列的实现是一个单向链表，当做栈来使用
     * 只包含了一个记录线程的thread属性和指向下一个节点的next属性
     */
    static final class WaitNode {
        volatile Thread thread;
        volatile WaitNode next;
        WaitNode() { thread = Thread.currentThread(); }
    }

    /**
     * 任务执行完成后，唤醒等待队列中的其他线程
     */
    private void finishCompletion() {
        for (WaitNode q; (q = waiters) != null;) {
            // waiters属性设置成null
            if (UNSAFE.compareAndSwapObject(this, waitersOffset, q, null)) {
                // 遍历链表中所有等待的线程，并唤醒他们
                for (;;) {
                    Thread t = q.thread;
                    if (t != null) {
                        q.thread = null;
                        // 唤醒等待队列中的线程
                        LockSupport.unpark(t);
                    }
                    WaitNode next = q.next;
                    if (next == null)
                        break;
                    q.next = null; // unlink to help gc
                    q = next;
                }
                break;
            }
        }
        // 空方法。用户可自定义善后工作
        done();

        callable = null;        // to reduce footprint
    }

    private int awaitDone(boolean timed, long nanos) throws InterruptedException {
        final long deadline = timed ? System.nanoTime() + nanos : 0L;
        WaitNode q = null;
        boolean queued = false;
        for (;;) {
            // 线程中断
            if (Thread.interrupted()) {
                removeWaiter(q);
                throw new InterruptedException();
            }

            int s = state;
            // 如果任务已经进入终止态（s > COMPLETING），我们就直接返回任务的状态;
            if (s > COMPLETING) {
                if (q != null)
                    q.thread = null;
                return s;
            }
            // 如果任务正在设置执行结果（s == COMPLETING），我们就让出当前线程的CPU资源继续等待
            else if (s == COMPLETING) // cannot time out yet
                Thread.yield();
            // 如果q现在还为null, 说明当前线程还没有进入等待队列,新建一个WaitNode,
            else if (q == null)
                q = new WaitNode();
            // 如果当前线程还没有入队
            else if (!queued)
                // 线程入队
                queued = UNSAFE.compareAndSwapObject(this, waitersOffset, q.next = waiters, q);
            else if (timed) {
                nanos = deadline - System.nanoTime();
                if (nanos <= 0L) {
                    removeWaiter(q);
                    return state;
                }
                LockSupport.parkNanos(this, nanos);
            }
            else
                // 线程被fututreTask挂起
                // 那么这个挂起的线程什么时候会被唤醒呢？有两种情况：
                //   任务执行完毕了，在finishCompletion方法中会唤醒所有在Treiber栈中等待的线程
                //   等待的线程自身因为被中断等原因而被唤醒。
                LockSupport.park(this);

        }
    }

    private void removeWaiter(WaitNode node) {
        if (node != null) {
            node.thread = null;
            retry:
            for (;;) {          // restart on removeWaiter race
                for (WaitNode pred = null, q = waiters, s; q != null; q = s) {
                    s = q.next;
                    if (q.thread != null)
                        pred = q;
                    else if (pred != null) {
                        pred.next = s;
                        if (pred.thread == null) // check for race
                            continue retry;
                    }
                    else if (!UNSAFE.compareAndSwapObject(this, waitersOffset,
                                                          q, s))
                        continue retry;
                }
                break;
            }
        }
    }

    // Unsafe mechanics
    private static final sun.misc.Unsafe UNSAFE;
    private static final long stateOffset;
    private static final long runnerOffset;
    private static final long waitersOffset;
    static {
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class<?> k = FutureTask.class;
            stateOffset = UNSAFE.objectFieldOffset(k.getDeclaredField("state"));
            runnerOffset = UNSAFE.objectFieldOffset(k.getDeclaredField("runner"));
            waitersOffset = UNSAFE.objectFieldOffset(k.getDeclaredField("waiters"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }

}
