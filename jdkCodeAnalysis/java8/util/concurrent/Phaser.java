package java.util.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

/**
 * @code
 * void runTasks(List<Runnable> tasks) {
 *   final Phaser phaser = new Phaser(1); // "1" to register self
 *   // create and start threads
 *   for (final Runnable task : tasks) {
 *     phaser.register();
 *     new Thread() {
 *       public void run() {
 *         phaser.arriveAndAwaitAdvance(); // await all creation
 *         task.run();
 *       }
 *     }.start();
 *   }
 *
 *   // allow threads to start and deregister self
 *   phaser.arriveAndDeregister();
 * }}
 *
 * @code
 * void startTasks(List<Runnable> tasks, final int iterations) {
 *   final Phaser phaser = new Phaser() {
 *     protected boolean onAdvance(int phase, int registeredParties) {
 *       return phase >= iterations || registeredParties == 0;
 *     }
 *   };
 *   phaser.register();
 *   for (final Runnable task : tasks) {
 *     phaser.register();
 *     new Thread() {
 *       public void run() {
 *         do {
 *           task.run();
 *           phaser.arriveAndAwaitAdvance();
 *         } while (!phaser.isTerminated());
 *       }
 *     }.start();
 *   }
 *   phaser.arriveAndDeregister(); // deregister self, don't wait
 * }}
 *
 * @code
 * void awaitPhase(Phaser phaser, int phase) {
 *   int p = phaser.register(); // assumes caller not already registered
 *   while (p < phase) {
 *     if (phaser.isTerminated())
 *       // ... deal with unexpected termination
 *     else
 *       p = phaser.arriveAndAwaitAdvance();
 *   }
 *   phaser.arriveAndDeregister();
 * }}
 *
 *
 * {@code
 * void build(Task[] tasks, int lo, int hi, Phaser ph) {
 *   if (hi - lo > TASKS_PER_PHASER) {
 *     for (int i = lo; i < hi; i += TASKS_PER_PHASER) {
 *       int j = Math.min(i + TASKS_PER_PHASER, hi);
 *       build(tasks, i, j, new Phaser(ph));
 *     }
 *   } else {
 *     for (int i = lo; i < hi; ++i)
 *       tasks[i] = new Task(ph);
 *       // assumes new Task(ph) performs ph.register()
 *   }
 * }}
 *
 */

/**
 * 1、Registration（注册）：
 *  跟其他barrier不同，在phaser上注册的parties会随着时间的变化而变化。
 *  任务可以随时注册(使用方法register,bulkRegister注册，或者由构造器确定初始parties)，并且在任何抵达点可以随意地撤销注册(方法arriveAndDeregister)。
 *  就像大多数基本的同步结构一样，注册和撤销只影响内部count；不会创建更深的内部记录，所以任务不能查询他们是否已经注册。
 *  (不过，可以通过继承来实现类似的记录)
 *
 * 2、Synchronization(同步机制)：
 *  和CyclicBarrier一样，Phaser也可以重复await。
 *  方法arriveAndAwaitAdvance的效果类似CyclicBarrier.await。
 *  phaser的每一代都有一个相关的phase number，初始值为0，
 *  当所有注册的任务都到达phaser时phase+1，到达最大值(Integer.MAX_VALUE)之后清零。
 *  使用phase number可以独立控制到达phaser和等待其他线程 的动作，通过下面两种类型的方法：
 *      Arrival(到达机制) arrive和arriveAndDeregister方法记录到达状态。这些方法不会阻塞，但是会返回一个相关的arrival phase number；
 *      Waiting(等待机制) awaitAdvance方法需要一个表示arrival phase number的参数，并且在phaser前进到与给定phase不同的phase时返回。
 *                      和CyclicBarrier不同，即使等待线程已经被中断，awaitAdvance方法也会一直等待。中断状态和超时时间同样可用，但是当任务等待中断或超时后未改变phaser的状态时会遭遇异常
 *
 * 3、Termination(终止机制)：
 *  可以用isTerminated方法检查Phaser的终止状态。
 *  在终止时，所有同步方法立刻返回一个负值。在终止时尝试注册也没有效果。当调用onAdvance返回true时Termination被触发。
 *  当deregistration操作使已注册的parties变为0时，onAdvance的默认实现就会返回true。
 *  也可以重写onAdvance方法来定义终止动作。forceTermination方法也可以释放等待线程并且允许它们终止
 *
 * 4、Tiering(分层结构)：
 *  Phaser支持分层结构(树状构造)来减少竞争。
 *  注册了大量parties的Phaser可能会因为同步竞争消耗很高的成本， 因此可以设置一些子Phaser来共享一个通用的parent。
 *  这样的话即使每个操作消耗了更多的开销，但是会提高整体吞吐量
 *
 * 5、Monitoring(状态监控)：
 *  由于同步方法可能只被已注册的parties调用，所以phaser的当前状态也可能被任何调用者监控。
 *  在任何时候，可以通过getRegisteredParties获取parties数，其中getArrivedParties方法返回已经到达当前phase的parties数
 *
 *
 */

/**
 * Phaser是一个可以重复利用的同步栅栏，功能上与CyclicBarrier和CountDownLatch相似，不过提供更加灵活的用法
 * Phaser支持树形结构
 * 把多个线程协作执行的任务划分为多个阶段，编程时需要明确各个阶段的任务，每个阶段都有指定个参与者，线程都可以随时注册并参与到某个阶段
 */
public class Phaser {

    /**
     * Primary state representation, holding four bit-fields:
     *
     * unarrived：还未到达的参与者数目       (bits  0-15)
     * parties：当前阶段总的参与者数目       (bits 16-31)
     * phase：屏障所处的阶段                (bits 32-62)
     * terminated：屏障是否终止             (bit  63 / sign)
     */

    private volatile long state;

    // 最大的参与者数目
    private static final int  MAX_PARTIES     = 0xffff;
    // 最大的阶段值
    private static final int  MAX_PHASE       = Integer.MAX_VALUE;
    // 参与者移位
    private static final int  PARTIES_SHIFT   = 16;
    // 阶段移位
    private static final int  PHASE_SHIFT     = 32;
    // 未到达参与者数掩码
    private static final int  UNARRIVED_MASK  = 0xffff;      // to mask ints
    // 总参与者数掩码
    private static final long PARTIES_MASK    = 0xffff0000L; // to mask longs
    private static final long COUNTS_MASK     = 0xffffffffL;
    // 终止位
    private static final long TERMINATION_BIT = 1L << 63;

    // some special values
    // 一个到达者
    private static final int  ONE_ARRIVAL     = 1;
    // 一个参与者
    private static final int  ONE_PARTY       = 1 << PARTIES_SHIFT;
    // 撤销一个参与者
    private static final int  ONE_DEREGISTER  = ONE_ARRIVAL|ONE_PARTY;
    // 0 个参与者，1 个达到者
    private static final int  EMPTY           = 1;


    private static int unarrivedOf(long s) {
        int counts = (int)s;
        return (counts == EMPTY) ? 0 : (counts & UNARRIVED_MASK);
    }

    private static int partiesOf(long s) {
        return (int)s >>> PARTIES_SHIFT;
    }

    private static int phaseOf(long s) {
        return (int)(s >>> PHASE_SHIFT);
    }

    private static int arrivedOf(long s) {
        int counts = (int)s;
        return (counts == EMPTY) ? 0 :
            (counts >>> PARTIES_SHIFT) - (counts & UNARRIVED_MASK);
    }

    // 此 Phaser 的 parent
    private final Phaser parent;

    // The root of phaser tree. Equals this if not in a tree
    private final Phaser root;

    // 阶段值为偶数时， Treiber 栈节点，阻塞线程驻留在节点上
    private final AtomicReference<QNode> evenQ;
    // 阶段值为奇数时， Treiber 栈节点，阻塞线程驻留在节点上
    private final AtomicReference<QNode> oddQ;

    private AtomicReference<QNode> queueFor(int phase) {
        return ((phase & 1) == 0) ? evenQ : oddQ;
    }

    private String badArrive(long s) {
        return "Attempted arrival of unregistered party for " + stateToString(s);
    }

    private String badRegister(long s) {
        return "Attempt to register more than " + MAX_PARTIES + " parties for " + stateToString(s);
    }

    /**
     * 通过位运算计算当前state、phaser等值
     * 然后直接使用自旋+CAS更新state值（state-=adjust）
     * 如果当前不是最后一个未到达的任务，直接返回当前phaser值
     * 如果当前是最后一个未到达的任务
     * 如果当前是root节点，判断是否需要终止phase（nextUnarrived == 0）r，然后CAS更新state，最后释放等待phase的线程
     * 如果是分层结构，并且已经没有下一代未到达的parties，则交由父节点处理doArrive逻辑，然后更新state为EMPTY
     */
    private int doArrive(int adjust) {
        final Phaser root = this.root;
        for (;;) {
            long s = (root == this) ? state : reconcileState();
            // 读取阶段值
            int phase = (int)(s >>> PHASE_SHIFT);
            // 如果无 root Phaser，即 root==this，则返回其状态值
            if (phase < 0)
                return phase;
            int counts = (int)s;
            // 计算未到达屏障的参与者数目
            int unarrived = (counts == EMPTY) ? 0 : (counts & UNARRIVED_MASK);
            // 如果都已经到达屏障，则抛出 IllegalStateException
            if (unarrived <= 0)
                throw new IllegalStateException(badArrive(s));
            if (UNSAFE.compareAndSwapLong(this, stateOffset, s, s-=adjust)) {
                // 表示当前为最后一个未到达的任务
                if (unarrived == 1) {
                    long n = s & PARTIES_MASK;  // base of next state
                    int nextUnarrived = (int)n >>> PARTIES_SHIFT;
                    if (root == this) {
                        if (onAdvance(phase, nextUnarrived))
                            n |= TERMINATION_BIT;
                        else if (nextUnarrived == 0)
                            n |= EMPTY;
                        else
                            n |= nextUnarrived;
                        int nextPhase = (phase + 1) & MAX_PHASE;
                        n |= (long)nextPhase << PHASE_SHIFT;
                        UNSAFE.compareAndSwapLong(this, stateOffset, s, n);
                        releaseWaiters(phase);
                    }
                    else if (nextUnarrived == 0) { // propagate deregistration
                        phase = parent.doArrive(ONE_DEREGISTER);
                        UNSAFE.compareAndSwapLong(this, stateOffset,
                                                  s, s | EMPTY);
                    }
                    else
                        phase = parent.doArrive(ONE_ARRIVAL);
                }
                return phase;
            }
        }
    }

    /**
     * （1）增加一个参与者，需要同时增加parties和unarrived两个数值，也就是state的中16位和低16位；
     * （2）如果是第一个参与者，则尝试原子更新state的值，如果成功了就退出；
     * （3）如果不是第一个参与者，则检查是不是在执行onAdvance()，如果是等待onAdvance()执行完成，
     *     如果否则尝试原子更新state的值，直到成功退出；
     * （4）等待onAdvance()完成是采用先自旋后进入队列排队的方式等待，减少线程上下文切换；
     */
    private int doRegister(int registrations) {
        // state应该加的值，相当于同时增加parties和unarrived
        long adjust = ((long)registrations << PARTIES_SHIFT) | registrations;
        final Phaser parent = this.parent;
        int phase;
        for (;;) {
            // state的值
            long s = (parent == null) ? state : reconcileState();
            // state的低32位，也就是parties和unarrived的值
            int counts = (int)s;
            // parties的值
            int parties = counts >>> PARTIES_SHIFT;
            // unarrived的值
            int unarrived = counts & UNARRIVED_MASK;
            if (registrations > MAX_PARTIES - parties)
                throw new IllegalStateException(badRegister(s));
            // 当前阶段phase
            phase = (int)(s >>> PHASE_SHIFT);
            if (phase < 0)
                break;
            // 不是第一个参与者
            if (counts != EMPTY) {                  // not 1st registration
                if (parent == null || reconcileState() == s) {
                    // unarrived等于0说明当前阶段正在执行onAdvance()方法，等待其执行完毕
                    if (unarrived == 0)             // wait out advance
                        root.internalAwaitAdvance(phase, null);
                    // 否则就修改state的值，增加adjust，如果成功就跳出循环
                    else if (UNSAFE.compareAndSwapLong(this, stateOffset, s, s + adjust))
                        break;
                }
            }
            // 是第一个参与者
            else if (parent == null) {              // 1st root registration
                // 计算state的值
                long next = ((long)phase << PHASE_SHIFT) | adjust;
                // 修改state的值，如果成功就跳出循环
                if (UNSAFE.compareAndSwapLong(this, stateOffset, s, next))
                    break;
            }
            else {
                // 多层级阶段的处理方式
                synchronized (this) {               // 1st sub registration
                    if (state == s) {               // recheck under lock
                        // 交给父节点完成注册
                        phase = parent.doRegister(1);
                        if (phase < 0)
                            //退出自旋，返回phase ，也就是负数
                            break;
                        // 走到此处，说明父节点注册成功了（phase大于0），while自旋，直到CAS修改成功
                        while (!UNSAFE.compareAndSwapLong(this, stateOffset, s, ((long)phase << PHASE_SHIFT) | adjust)) {
                            s = state;
                            phase = (int)(root.state >>> PHASE_SHIFT);
                            // assert (int)s == EMPTY;
                        }
                        break;
                    }
                }
            }
        }
        return phase;
    }

    private long reconcileState() {
        final Phaser root = this.root;
        long s = state;
        if (root != this) {
            int phase, p;
            // CAS to root phase with current parties, tripping unarrived
            while ((phase = (int)(root.state >>> PHASE_SHIFT)) !=
                   (int)(s >>> PHASE_SHIFT) &&
                   !UNSAFE.compareAndSwapLong
                   (this, stateOffset, s,
                    s = (((long)phase << PHASE_SHIFT) |
                         ((phase < 0) ? (s & COUNTS_MASK) :
                          (((p = (int)s >>> PARTIES_SHIFT) == 0) ? EMPTY :
                           ((s & PARTIES_MASK) | p))))))
                s = state;
        }
        return s;
    }

    public Phaser() {
        this(null, 0);
    }

    public Phaser(int parties) {
        this(null, parties);
    }

    public Phaser(Phaser parent) {
        this(parent, 0);
    }

    public Phaser(Phaser parent, int parties) {
        if (parties >>> PARTIES_SHIFT != 0)
            throw new IllegalArgumentException("Illegal number of parties");
        int phase = 0;
        this.parent = parent;
        if (parent != null) {
            final Phaser root = parent.root;
            this.root = root;
            this.evenQ = root.evenQ;
            this.oddQ = root.oddQ;
            if (parties != 0)
                phase = parent.doRegister(1);
        }
        else {
            this.root = this;
            this.evenQ = new AtomicReference<QNode>();
            this.oddQ = new AtomicReference<QNode>();
        }
        this.state = (parties == 0) ? (long)EMPTY :
            ((long)phase << PHASE_SHIFT) |
            ((long)parties << PARTIES_SHIFT) |
            ((long)parties);
    }

    // register方法注册一个party数
    public int register() {
        return doRegister(1);
    }
    // bulkRegister方法可以注册数加上已经注册的，最大不超过MAX_PARTIES
    public int bulkRegister(int parties) {
        if (parties < 0)
            throw new IllegalArgumentException();
        if (parties == 0)
            return getPhase();
        return doRegister(parties);
    }

    // 到达当前阶段，此方法不会阻塞
    public int arrive() {
        return doArrive(ONE_ARRIVAL);
    }

    public int arriveAndDeregister() {
        return doArrive(ONE_DEREGISTER);
    }

    /**
     * 当前线程当前阶段执行完毕，等待其它线程完成当前阶段。
     * 如果当前线程是该阶段最后一个到达的，则当前线程会执行onAdvance()方法，并唤醒其它线程进入下一个阶段。
     */
    public int arriveAndAwaitAdvance() {
        /**
         * （1）修改state中unarrived部分的值减1；
         * （2）如果不是最后一个到达的，则调用internalAwaitAdvance()方法自旋或排队等待；
         * （3）如果是最后一个到达的，则调用onAdvance()方法，然后修改state的值为下一阶段对应的值，并唤醒其它等待的线程；
         * （4）返回下一阶段的值；
         */
        final Phaser root = this.root;
        for (;;) {
            long s = (root == this) ? state : reconcileState();
            int phase = (int)(s >>> PHASE_SHIFT);
            if (phase < 0)
                return phase;
            int counts = (int)s;
            int unarrived = (counts == EMPTY) ? 0 : (counts & UNARRIVED_MASK);
            if (unarrived <= 0)
                throw new IllegalStateException(badArrive(s));
            if (UNSAFE.compareAndSwapLong(this, stateOffset, s, s -= ONE_ARRIVAL)) {
                // 如果不是最后一个到达的，则调用internalAwaitAdvance()方法自旋或进入队列等待
                if (unarrived > 1)
                    return root.internalAwaitAdvance(phase, null);
                // 到这里说明是最后一个到达的参与者
                if (root != this)
                    return parent.arriveAndAwaitAdvance();
                long n = s & PARTIES_MASK;  // base of next state
                // parties的值，即下一次需要到达的参与者数量
                int nextUnarrived = (int)n >>> PARTIES_SHIFT;
                // 执行onAdvance()方法，返回true表示下一阶段参与者数量为0了，也就是结束了
                if (onAdvance(phase, nextUnarrived))
                    n |= TERMINATION_BIT;
                else if (nextUnarrived == 0)
                    n |= EMPTY;
                else
                    n |= nextUnarrived;
                // 下一个阶段等待当前阶段加1
                int nextPhase = (phase + 1) & MAX_PHASE;
                n |= (long)nextPhase << PHASE_SHIFT;
                if (!UNSAFE.compareAndSwapLong(this, stateOffset, s, n))
                    return (int)(state >>> PHASE_SHIFT); // terminated
                // 唤醒其它参与者并进入下一个阶段
                releaseWaiters(phase);
                // 返回下一阶段的值
                return nextPhase;
            }
        }
    }

    public int awaitAdvance(int phase) {
        final Phaser root = this.root;
        long s = (root == this) ? state : reconcileState();
        int p = (int)(s >>> PHASE_SHIFT);
        if (phase < 0)
            return phase;
        if (p == phase)
            return root.internalAwaitAdvance(phase, null);
        return p;
    }

    public int awaitAdvanceInterruptibly(int phase)
        throws InterruptedException {
        final Phaser root = this.root;
        long s = (root == this) ? state : reconcileState();
        int p = (int)(s >>> PHASE_SHIFT);
        if (phase < 0)
            return phase;
        if (p == phase) {
            QNode node = new QNode(this, phase, true, false, 0L);
            p = root.internalAwaitAdvance(phase, node);
            if (node.wasInterrupted)
                throw new InterruptedException();
        }
        return p;
    }

    public int awaitAdvanceInterruptibly(int phase, long timeout, TimeUnit unit)
        throws InterruptedException, TimeoutException {
        long nanos = unit.toNanos(timeout);
        final Phaser root = this.root;
        long s = (root == this) ? state : reconcileState();
        int p = (int)(s >>> PHASE_SHIFT);
        if (phase < 0)
            return phase;
        if (p == phase) {
            QNode node = new QNode(this, phase, true, true, nanos);
            p = root.internalAwaitAdvance(phase, node);
            if (node.wasInterrupted)
                throw new InterruptedException();
            else if (p == phase)
                throw new TimeoutException();
        }
        return p;
    }

    public void forceTermination() {
        // Only need to change root state
        final Phaser root = this.root;
        long s;
        while ((s = root.state) >= 0) {
            if (UNSAFE.compareAndSwapLong(root, stateOffset,
                                          s, s | TERMINATION_BIT)) {
                // signal all threads
                releaseWaiters(0); // Waiters on evenQ
                releaseWaiters(1); // Waiters on oddQ
                return;
            }
        }
    }

    public final int getPhase() {
        return (int)(root.state >>> PHASE_SHIFT);
    }

    public int getRegisteredParties() {
        return partiesOf(state);
    }

    public int getArrivedParties() {
        return arrivedOf(reconcileState());
    }

    public int getUnarrivedParties() {
        return unarrivedOf(reconcileState());
    }

    public Phaser getParent() {
        return parent;
    }

    public Phaser getRoot() {
        return root;
    }

    public boolean isTerminated() {
        return root.state < 0L;
    }

    protected boolean onAdvance(int phase, int registeredParties) {
        return registeredParties == 0;
    }

    public String toString() {
        return stateToString(reconcileState());
    }

    private String stateToString(long s) {
        return super.toString() + "[phase = " + phaseOf(s) + " parties = " + partiesOf(s) + " arrived = " + arrivedOf(s) + "]";
    }

    // Waiting mechanics

    private void releaseWaiters(int phase) {
        QNode q;   // first element of queue
        Thread t;  // its thread
        AtomicReference<QNode> head = (phase & 1) == 0 ? evenQ : oddQ;
        while ((q = head.get()) != null &&
               q.phase != (int)(root.state >>> PHASE_SHIFT)) {
            if (head.compareAndSet(q, q.next) && (t = q.thread) != null) {
                q.thread = null;
                LockSupport.unpark(t);
            }
        }
    }

    private int abortWait(int phase) {
        AtomicReference<QNode> head = (phase & 1) == 0 ? evenQ : oddQ;
        for (;;) {
            Thread t;
            QNode q = head.get();
            int p = (int)(root.state >>> PHASE_SHIFT);
            if (q == null || ((t = q.thread) != null && q.phase == p))
                return p;
            if (head.compareAndSet(q, q.next) && t != null) {
                q.thread = null;
                LockSupport.unpark(t);
            }
        }
    }

    private static final int NCPU = Runtime.getRuntime().availableProcessors();

    static final int SPINS_PER_ARRIVAL = (NCPU < 2) ? 1 : 1 << 8;

    // 等待onAdvance()方法执行完毕
    // 原理是先自旋一定次数，如果进入下一个阶段，这个方法直接就返回了，
    // 如果自旋一定次数后还没有进入下一个阶段，则当前线程入队列，等待onAdvance()执行完毕唤醒
    private int internalAwaitAdvance(int phase, QNode node) {
        // 保证队列为空
        releaseWaiters(phase-1);          // ensure old queue clean
        boolean queued = false;           // true when node is enqueued
        int lastUnarrived = 0;            // to increase spins upon change
        int spins = SPINS_PER_ARRIVAL;
        long s;
        int p;
        // 检查当前阶段是否变化，如果变化了说明进入下一个阶段了，这时候就没有必要自旋
        while ((p = (int)((s = state) >>> PHASE_SHIFT)) == phase) {
            if (node == null) {           // spinning in noninterruptible mode
                int unarrived = (int)s & UNARRIVED_MASK;
                if (unarrived != lastUnarrived &&
                    (lastUnarrived = unarrived) < NCPU)
                    spins += SPINS_PER_ARRIVAL;
                boolean interrupted = Thread.interrupted();
                if (interrupted || --spins < 0) { // need node to record intr
                    node = new QNode(this, phase, false, false, 0L);
                    node.wasInterrupted = interrupted;
                }
            }
            else if (node.isReleasable()) // done or aborted
                break;
            // 入队
            else if (!queued) {           // push onto queue
                AtomicReference<QNode> head = (phase & 1) == 0 ? evenQ : oddQ;
                QNode q = node.next = head.get();
                if ((q == null || q.phase == phase) &&
                    (int)(state >>> PHASE_SHIFT) == phase) // avoid stale enq
                    queued = head.compareAndSet(q, node);
            }
            else {
                try {
                    ForkJoinPool.managedBlock(node);
                } catch (InterruptedException ie) {
                    node.wasInterrupted = true;
                }
            }
        }

        if (node != null) {
            if (node.thread != null)
                node.thread = null;       // avoid need for unpark()
            if (node.wasInterrupted && !node.interruptible)
                Thread.currentThread().interrupt();
            if (p == phase && (p = (int)(state >>> PHASE_SHIFT)) == phase)
                return abortWait(phase); // possibly clean up on abort
        }
        releaseWaiters(phase);
        return p;
    }

    static final class QNode implements ForkJoinPool.ManagedBlocker {
        final Phaser phaser;
        final int phase;
        final boolean interruptible;
        final boolean timed;
        boolean wasInterrupted;
        long nanos;
        final long deadline;
        volatile Thread thread; // nulled to cancel wait
        // 单向链表
        QNode next;

        QNode(Phaser phaser, int phase, boolean interruptible,
              boolean timed, long nanos) {
            this.phaser = phaser;
            this.phase = phase;
            this.interruptible = interruptible;
            this.nanos = nanos;
            this.timed = timed;
            this.deadline = timed ? System.nanoTime() + nanos : 0L;
            thread = Thread.currentThread();
        }

        public boolean isReleasable() {
            if (thread == null)
                return true;
            if (phaser.getPhase() != phase) {
                thread = null;
                return true;
            }
            if (Thread.interrupted())
                wasInterrupted = true;
            if (wasInterrupted && interruptible) {
                thread = null;
                return true;
            }
            if (timed) {
                if (nanos > 0L) {
                    nanos = deadline - System.nanoTime();
                }
                if (nanos <= 0L) {
                    thread = null;
                    return true;
                }
            }
            return false;
        }

        public boolean block() {
            if (isReleasable())
                return true;
            else if (!timed)
                LockSupport.park(this);
            else if (nanos > 0L)
                LockSupport.parkNanos(this, nanos);
            return isReleasable();
        }
    }

    // Unsafe mechanics

    private static final sun.misc.Unsafe UNSAFE;
    private static final long stateOffset;
    static {
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class<?> k = Phaser.class;
            stateOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("state"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
