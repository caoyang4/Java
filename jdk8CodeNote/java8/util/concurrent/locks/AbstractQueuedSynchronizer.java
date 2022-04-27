
package java.util.concurrent.locks;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import sun.misc.Unsafe;

/**
 * AQS中，排在阻塞队列第一位的使用自旋等待，而排在后面的线程则挂起。
 * AQS是抽象的队列式同步器框架，是除了java自带的synchronized关键字之外的锁机制。
 * 其底层采用乐观锁，大量使用了CAS操作，同时采用自旋方式重试，以实现轻量级和高效地获取锁。
 */
public abstract class AbstractQueuedSynchronizer extends AbstractOwnableSynchronizer implements java.io.Serializable {

    private static final long serialVersionUID = 7373984972572414691L;

    protected AbstractQueuedSynchronizer() { }

    /**
     *      +------+  prev +-----+       +-----+
     * head |      | <---- |     | <---- |     |  tail
     *      +------+       +-----+       +-----+
     * CLH队列：一个虚拟的双向队列，虚拟的双向队列即不存在队列实例，仅存在节点之间的关联关系。
     * AQS是将每一条请求共享资源的线程封装成一个CLH锁队列的一个结点（Node），来实现锁的分配。
     * AQS维护了一个CLH队列，以及一个用volatile修饰的state（共享资源），其中state由线程通过CAS去改变
     *
     * AQS本身是基于【模板方法模式】设计的，在使用时无需关注具体的维护和实现（如获取资源失败、入队、出队、唤醒等），只需要重写获取和释放共享资源state的方法即可。
     * 目前AQS定义了两种资源共享方式：Exclusive（独占，如：ReentrantLock）和Share（共享，如：Semaphore、CountDownLatch）
     * 目前实现了AQS的组件有：ReentrantLock、ReentrantReadWriteLock、Semaphore、CountDownLatch、CyclicBarrier
     */
    static final class Node {
        // 共享模式
        static final Node SHARED = new Node();
        // 独占模式
        static final Node EXCLUSIVE = null;

        // 当前节点已取消获取锁，也是唯一大于0的状态。
        static final int CANCELLED =  1;
        // 表示当前节点的后继节点在等待唤醒，在后继节点找到安全点时，会更新其前驱的状态为此
        static final int SIGNAL    = -1;
        // 节点调用了Condition的await()，进入条件队列，正在等待
        static final int CONDITION = -2;
        // 共享模式下，可以唤醒多个后继节点
        static final int PROPAGATE = -3;

        // 节点的状态, 初始默认状态 0
        volatile int waitStatus;

        // 前驱节点
        volatile Node prev;

        // 后继节点
        volatile Node next;

        // 节点包装的线程
        volatile Thread thread;

        Node nextWaiter;

        final boolean isShared() {
            return nextWaiter == SHARED;
        }

        final Node predecessor() throws NullPointerException {
            Node p = prev;
            if (p == null)
                throw new NullPointerException();
            else
                return p;
        }

        Node() {    // Used to establish initial head or SHARED marker
        }

        Node(Thread thread, Node mode) {     // Used by addWaiter
            this.nextWaiter = mode;
            this.thread = thread;
        }

        Node(Thread thread, int waitStatus) { // Used by Condition
            this.waitStatus = waitStatus;
            this.thread = thread;
        }
    }

    // 头结点
    // head头节点是一个dummy结点或者是当前持有锁的线程，真正的等待线程是从第二个节点开始的。
    // head所指向的Node的thread属性永远是null
    private transient volatile Node head;

    // 尾结点
    private transient volatile Node tail;

    // 该属性表示了锁的状态，state为0表示锁没有被占用，state大于0表示当前已有线程持有该锁，大于1则说明存在重入的情况
    private volatile int state;

    protected final int getState() {
        return state;
    }

    protected final void setState(int newState) {
        state = newState;
    }

    // state 通过 CAS 进行设置
    protected final boolean compareAndSetState(int expect, int update) {
        return unsafe.compareAndSwapInt(this, stateOffset, expect, update);
    }

    static final long spinForTimeoutThreshold = 1000L;

    // cas方式入队，加到队尾
    private Node enq(final Node node) {
        for (;;) {
            Node t = tail;
            // 队列懒初始化
            if (t == null) {
                // 初始化时使用new Node()方法新建了一个dummy节点
                // head节点不代表任何线程，它就是一个空节点
                if (compareAndSetHead(new Node()))
                    tail = head;
            } else {
                // 此处并发条件下可能出现尾分叉，多个节点的prev指向t成功，但cas设置尾部失败，一直循环直到设置成功
                node.prev = t;
                if (compareAndSetTail(t, node)) {
                    t.next = node;
                    return t;
                }
            }
        }
    }


    private Node addWaiter(Node mode) {
        // 将线程包装为Node，初始状态为0
        Node node = new Node(Thread.currentThread(), mode);
        Node pred = tail;
        if (pred != null) {
            // 如果队列不为空, 则用CAS方式将当前节点设为尾节点
            node.prev = pred;
            if (compareAndSetTail(pred, node)) {
                pred.next = node;
                return node;
            }
        }
        // 队列为空，或者 cas 失败，将节点插入队列
        enq(node);
        return node;
    }

    private void setHead(Node node) {
        head = node;
        node.thread = null;
        node.prev = null;
    }

    private void unparkSuccessor(Node node) {
        int ws = node.waitStatus;
        if (ws < 0)
            // 如果head节点的ws比0小, 则直接将它设为0
            compareAndSetWaitStatus(node, ws, 0);
        // 通常情况下, 要唤醒的节点就是自己的后继节点
        // 如果后继节点存在且也在等待锁, 那就直接唤醒它
        // 但是有可能存在后继节点取消等待锁的情况
        // 此时从尾节点开始向前找起, 直到找到距离head节点最近的ws<=0的节点
        Node s = node.next;
        if (s == null || s.waitStatus > 0) {
            // 没有后继节点，或者后继节点取消获取锁
            s = null;
            // 从尾部向前找距离head节点最近且处于等待的节点
            // 从后往前找的目的其实是为了照顾刚刚加入到队列中的节点，
            // 如果从前往后找，出现尾分叉情况，可能会找不到尾结点
            // 总结来说，之所以从后往前遍历是因为，我们是处于多线程并发的条件下的，
            // 如果一个节点的next属性为null, 并不能保证它就是尾节点
            // （可能是因为新加的尾节点还没来得及执行pred.next = node）,
            // 但是一个节点如果能入队, 则它的prev属性一定是有值的,所以反向查找一定是最精确的
            for (Node t = tail; t != null && t != node; t = t.prev)
                if (t.waitStatus <= 0)
                    s = t;
        }
        if (s != null)
            LockSupport.unpark(s.thread);
    }

    private void doReleaseShared() {
        // 在之前的setHeadAndPropagate方法中可能已经调用过该方法了，也就是说它可能会被同一个头节点调用两次，
        // 也有可能在我们从releaseShared方法中调用它时，在共享锁中，持有共享锁的线程可以有多个，这些线程都可以调用releaseShared方法释放锁，当前的头节点已经易主
        for (;;) {
            // 在共享锁中，当头节点发生变化时，是会回到循环中再立即唤醒head节点的下一个节点的。
            // 也就是说，在当前节点完成唤醒后继节点的任务之后将要退出时，
            // 如果发现被唤醒后继节点已经成为了新的头节点，则会立即触发唤醒head节点的下一个节点的操作，如此周而复始
            Node h = head;
            // 队列中至少有两个节点
            if (h != null && h != tail) {
                int ws = h.waitStatus;
                // 如果当前ws值为Node.SIGNAL，则说明后继节点需要唤醒
                if (ws == Node.SIGNAL) {
                    if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0))
                        continue;
                    // 唤醒后续节点
                    unparkSuccessor(h);
                }
                // 该else if分支描述了一个极其严苛且短暂的状态：
                // 首先，大前提是队列里至少有两个节点
                // 其次，要执行到else if语句，说明我们跳过了前面的if条件，说明头节点是刚刚成为头节点的，它的waitStatus值还为0，
                // 尾节点是在这之后刚刚加进来的，它需要执行shouldParkAfterFailedAcquire，将它的前驱节点（即头节点）的waitStatus值修改为Node.SIGNAL，
                // 但是目前这个修改操作还没有来的及执行。这种情况使我们得以进入else if的前半部分else if (ws == 0 &&
                // 紧接着，要满足!compareAndSetWaitStatus(h, 0, Node.PROPAGATE)这一条件，说明此时头节点的waitStatus已经不是0了，
                // 这说明之前那个没有来得及执行的 在shouldParkAfterFailedAcquire将前驱节点的的waitStatus值修改为Node.SIGNAL的操作现在执行完了。


                // ws为0是指当前队列的最后一个节点成为了头节点，因为每次新的节点加进来，在挂起前一定会将自己的前驱节点的waitStatus修改成Node.SIGNAL的
                else if (ws == 0 &&
                         // cas失败，说明就在执行这个操作的瞬间，ws此时已经不为0了，说明有新的节点入队了，ws的值被改为了Node.SIGNAL，
                         // 此时我们将调用continue，在下次循环中直接将这个刚刚新入队但准备挂起的线程唤醒
                         !compareAndSetWaitStatus(h, 0, Node.PROPAGATE))

                    continue;                // loop on failed CAS
            }
            if (h == head)
                // 只有在当前head没有易主时，才会退出，否则继续循环
                break;
        }
    }

    private void setHeadAndPropagate(Node node, int propagate) {
        // 此方法，线程已经获取到锁，线程安全
        Node h = head; // Record old head for check below
        setHead(node);

        // 在ReentrantReadWriteLock中走到setHeadAndPropagate，只可能是propagate > 0，所以后面判断旧、新head的逻辑就被短路了。
        // 在Semaphore中走到setHeadAndPropagate，propagate是可以等于0的，表示没有剩余资源了，故propagate > 0不满足，往后判断。
        // 首先判断旧head是否为null，一般情况下是不可能是等于null，除非旧head刚好被gc了。h == null不满足，继续判断h.waitStatus < 0，h.waitStatus可能等于0，可能等于-3。
        if (propagate > 0 || h == null || h.waitStatus < 0 ||
            // 首先判断新head是否为空，一般情况下新head不为空，(h = head) == null不满足，判断h.waitStatus < 0，h.waitStatus可能等于0，可能小于0（-3 or -1）
            (h = head) == null || h.waitStatus < 0) {
            // 在共享锁模式下，锁可以被多个线程所共同持有，
            // 既然当前线程已经拿到共享锁了，那么就可以直接通知后继节点来拿锁，而不必等待锁被释放的时候再通知
            Node s = node.next;
            if (s == null || s.isShared())
                doReleaseShared();
        }
    }

    /**
     * cancelAcquire方法不仅是取消了当前节点的排队，还会同时将当前节点之前的那些已经CANCEL掉的节点移出队列
     * 如果要cancel的节点已经是尾节点了，则在我们后面并没有节点需要唤醒，我们只需要从当前节点(即尾节点)开始向前遍历，找到所有已经cancel的节点，将他们移出队列即可
     * 如果要cancel的节点后面还有别的节点，并且我们找到的pred节点处于正常等待状态，我们还是直接将从当前节点开始，到pred节点直接的所有节点，全部移出队列，这里并不需要唤醒当前节点的后继节点，因为它已经接在了pred的后面，pred的waitStatus已经被置为SIGNAL，它会负责唤醒后继节点
     * 如果上面的条件不满足，按说明当前节点往前已经没有在等待中的线程了，我们就直接将后继节点唤醒。
     */
    private void cancelAcquire(Node node) {
        // 处理当前取消节点的状态；
        // 将当前取消节点的前置非取消节点和后置非取消节点"链接"起来；
        // 如果前置节点释放了锁，那么当前取消节点承担起后续节点的唤醒职责。
        if (node == null)
            return;

        node.thread = null;

        // 将当前取消节点的前置非取消节点和后置非取消节点"链接"起来；
        // pred表示从当前节点向前遍历所找到的第一个没有被cancel的节点
        Node pred = node.prev;
        while (pred.waitStatus > 0)
            node.prev = pred = pred.prev;
        Node predNext = pred.next;

        node.waitStatus = Node.CANCELLED;
        // 并发场景下，可能失败，可能会有新节点加入
        if (node == tail && compareAndSetTail(node, pred)) {
            compareAndSetNext(pred, predNext, null);
        } else {
            int ws;
            if (pred != head &&
                ((ws = pred.waitStatus) == Node.SIGNAL ||
                 (ws <= 0 && compareAndSetWaitStatus(pred, ws, Node.SIGNAL))) &&
                pred.thread != null) {
                Node next = node.next;
                if (next != null && next.waitStatus <= 0)
                    compareAndSetNext(pred, predNext, next);
            } else {
                unparkSuccessor(node);
            }

            node.next = node; // help GC
        }
    }


    /**
     * 如果为前驱节点的waitStatus值为 Node.SIGNAL 则直接返回 true
     * 如果为前驱节点的waitStatus值为 Node.CANCELLED (ws > 0), 则跳过那些节点, 重新寻找正常等待中的前驱节点，然后排在它后面，返回false
     * 其他情况, 将前驱节点的状态改为 Node.SIGNAL, 返回false
     */
    private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
        // 获得前驱节点的waitStatus
        int ws = pred.waitStatus;
        if (ws == Node.SIGNAL)
            // 前驱节点的状态已经是SIGNAL，当前节点等着就好
            return true;
        if (ws > 0) {
            // 当前节点的 ws > 0, 则为 Node.CANCELLED 说明前驱节点已经取消了等待锁(由于超时或者中断等原因)
            // 既然前驱节点不等了, 那就继续往前找, 直到找到一个还在等待锁的节点
            // 然后我们跨过这些不等待锁的节点, 直接排在等待锁的节点的后面
            do {
                // 此处就是需要双向链表的本质
                // 需要前向找到唤醒当前节点的前驱节点
                node.prev = pred = pred.prev;
            } while (pred.waitStatus > 0);
            pred.next = node;
        } else {
            // 前驱节点的状态既不是SIGNAL，也不是CANCELLED
            // 用CAS设置前驱节点的ws为 Node.SIGNAL
            // 若下次还没有争抢到锁，就会park等待
            compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
        }
        // 返回继续争抢锁
        return false;
    }

    static void selfInterrupt() {
        Thread.currentThread().interrupt();
    }


    private final boolean parkAndCheckInterrupt() {
        LockSupport.park(this);
        // 走到此处，线程被唤醒
        // 返回当前正在执行的线程的中断状态，并清除它
        return Thread.interrupted();
    }

    final boolean acquireQueued(final Node node, int arg) {
        boolean failed = true;
        try {
            boolean interrupted = false;
            for (;;) {
                final Node p = node.predecessor();
                //  在当前节点的前驱就是HEAD节点时, 再次尝试获取锁
                if (p == head && tryAcquire(arg)) {
                    // 获取锁成功，当前节点置为 head 节点
                    // 丢弃原来的head，将head指向已经获得了锁的node。
                    // 该node的thread属性置为null了，又导致了这个新的head节点又成为了一个哑节点，它不代表任何线程
                    // 为什么要这样做呢，因为在tryAcquire调用成功后，exclusiveOwnerThread属性就已经记录了当前获取锁的线程了，此处没有必要再记录。
                    // 这某种程度上就是将当前线程从等待队列里面拿出来了，是一个变相的出队操作
                    // setHead没有用 cas 方式，此处没有竞争，因为 tryAcquire 成功，已经获得了锁
                    setHead(node);
                    p.next = null; // help GC
                    failed = false;
                    // 抢到锁，先执行finally代码块，然后返回
                    return interrupted;
                }
                //在获取锁失败后, 判断是否需要把当前线程挂起
                // 当shouldParkAfterFailedAcquire返回false后，会继续回到循环中再次尝试获取锁
                // 这是因为此时我们的前驱节点可能已经变了!（搞不好前驱节点就变成head节点了呢）
                if (shouldParkAfterFailedAcquire(p, node) &&
                    // 线程被挂起  LockSupport.park
                    // 如果没有中断，被唤醒的线程进行新一轮的抢锁
                    parkAndCheckInterrupt())
                    // 只设置中断标志，抢锁过程中，不响应中断
                    interrupted = true;
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }


    private void doAcquireInterruptibly(int arg)
        throws InterruptedException {
        final Node node = addWaiter(Node.EXCLUSIVE);
        boolean failed = true;
        try {
            for (;;) {
                final Node p = node.predecessor();
                if (p == head && tryAcquire(arg)) {
                    setHead(node);
                    p.next = null; // help GC
                    failed = false;
                    return;
                }
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())
                    // 与acquireQueued方法的不同之处
                    // 响应中断
                    throw new InterruptedException();
            }
        } finally {
            if (failed)
                // 抛出中断异常后，执行该方法
                cancelAcquire(node);
        }
    }


    private boolean doAcquireNanos(int arg, long nanosTimeout)
            throws InterruptedException {
        if (nanosTimeout <= 0L)
            return false;
        final long deadline = System.nanoTime() + nanosTimeout;
        final Node node = addWaiter(Node.EXCLUSIVE);
        boolean failed = true;
        try {
            for (;;) {
                final Node p = node.predecessor();
                if (p == head && tryAcquire(arg)) {
                    setHead(node);
                    p.next = null; // help GC
                    failed = false;
                    return true;
                }
                nanosTimeout = deadline - System.nanoTime();
                if (nanosTimeout <= 0L)
                    return false;
                if (shouldParkAfterFailedAcquire(p, node) &&
                    nanosTimeout > spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanosTimeout);
                if (Thread.interrupted())
                    throw new InterruptedException();
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }


    private void doAcquireShared(int arg) {
        // 将线程 node 节点设置为共享模式
        // 共享模式节点的 nextWaiter 存在，static final Node SHARED = new Node();
        // 在条件队列中，nextWaiter是指向条件队列中的下一个节点的，它将条件队列中的节点串起来，构成了单链表
        // 在sync queue队列中，我们只用prev,next属性来串联节点，形成双向链表，
        // nextWaiter属性在这里只起到一个标记作用，不会串联节点，
        // 这里不要被Node SHARED = new Node()所指向的空节点迷惑，这个空节点并不属于sync queue，不代表任何线程，
        // 它只起到标记作用，仅仅用作判断节点是否处于共享模式的依据

        final Node node = addWaiter(Node.SHARED);
        boolean failed = true;
        try {
            boolean interrupted = false;
            for (;;) {
                // 前驱节点
                final Node p = node.predecessor();
                if (p == head) {
                    int r = tryAcquireShared(arg);
                    if (r >= 0) {
                        setHeadAndPropagate(node, r);
                        p.next = null; // help GC
                        if (interrupted)
                            selfInterrupt();
                        failed = false;
                        return;
                    }
                }
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())
                    interrupted = true;
            }
        } finally {
            // 出现异常，取消获取锁
            if (failed)
                cancelAcquire(node);
        }
    }


    private void doAcquireSharedInterruptibly(int arg)
        throws InterruptedException {
        final Node node = addWaiter(Node.SHARED);
        boolean failed = true;
        try {
            for (;;) {
                final Node p = node.predecessor();
                if (p == head) {
                    int r = tryAcquireShared(arg);
                    if (r >= 0) {
                        setHeadAndPropagate(node, r);
                        p.next = null; // help GC
                        failed = false;
                        return;
                    }
                }
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())
                    throw new InterruptedException();
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }


    private boolean doAcquireSharedNanos(int arg, long nanosTimeout)
            throws InterruptedException {
        if (nanosTimeout <= 0L)
            return false;
        final long deadline = System.nanoTime() + nanosTimeout;
        final Node node = addWaiter(Node.SHARED);
        boolean failed = true;
        try {
            for (;;) {
                final Node p = node.predecessor();
                if (p == head) {
                    int r = tryAcquireShared(arg);
                    if (r >= 0) {
                        setHeadAndPropagate(node, r);
                        p.next = null; // help GC
                        failed = false;
                        return true;
                    }
                }
                nanosTimeout = deadline - System.nanoTime();
                if (nanosTimeout <= 0L)
                    return false;
                if (shouldParkAfterFailedAcquire(p, node) &&
                    nanosTimeout > spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanosTimeout);
                if (Thread.interrupted())
                    throw new InterruptedException();
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }


    protected boolean tryAcquire(int arg) {
        throw new UnsupportedOperationException();
    }


    protected boolean tryRelease(int arg) {
        throw new UnsupportedOperationException();
    }


    protected int tryAcquireShared(int arg) {
        throw new UnsupportedOperationException();
    }


    protected boolean tryReleaseShared(int arg) {
        throw new UnsupportedOperationException();
    }


    protected boolean isHeldExclusively() {
        throw new UnsupportedOperationException();
    }


    public final void acquire(int arg) {
        // tryAcquire方法由继承AQS的子类实现, 为获取锁的具体逻辑。
        if (!tryAcquire(arg) &&
            // 执行addWaiter
            // 传入的mode值为Node.EXCLUSIVE，所以节点的nextWaiter属性被设为null
            // 每一个处于独占锁模式下的节点，它的nextWaiter一定是null。

            // 执行acquireQueued
            // (1) 能执行到该方法, 说明addWaiter 方法已经成功将包装了当前Thread的节点添加到了等待队列的队尾
            // (2) 该方法中将再次尝试去获取锁: 基于当前节点的前驱节点就是HEAD节点
            // (3) 在再次尝试获取锁失败后, 判断是否需要把当前线程挂起
            // 独占模式节点的 nextWaiter 不存在，static final Node EXCLUSIVE = null;
            acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
            // selfInterrupt 执行的前提是 acquireQueued(addWaiter(Node.EXCLUSIVE), arg)方法返回 true。
            // 这个方法返回的是线程在获取锁的过程中是否发生过中断，返回 true 则证明发生过中断。
            // 所以 acquire 中的 selfInterrupt 其实是对获取锁的过程中发生过的中断的补充。
            // 走到此处，说明在获取锁过程中，线程发生了中断 interrupted = true;

            // 具体来说，当我们从LockSupport.park(this)处被唤醒，我们并不知道是因为什么原因被唤醒，
            // 可能是因为别的线程释放了锁，调用了 LockSupport.unpark(s.thread)，也有可能是因为当前线程在等待中被中断了，
            // 因此我们通过Thread.interrupted()方法检查了当前线程的中断标志，并将它记录下来，
            // 在我们最后返回acquire方法后，如果发现当前线程曾经被中断过，那我们就把当前线程再中断一次。

            // 为什么不直接用 isInterrupt()判断，
            // 是因为在获取锁的过程中，是通过 park+ 死循环实现的。
            // 每次 park 被唤醒之后都会重置中断状态，所以拿到锁的时候中断状态都是被重置后的

            // 从上面的代码中我们知道，即使线程在等待资源的过程中被中断唤醒，
            // 它还是会不依不饶的再抢锁，直到它抢到锁为止。也就是说，它是不响应这个中断的，仅仅是记录下自己被人中断过。
            selfInterrupt();
    }


    public final void acquireInterruptibly(int arg)
            throws InterruptedException {
        if (Thread.interrupted()) // interrupted设置中断标志
            // 抛出中断异常
            throw new InterruptedException();
        if (!tryAcquire(arg))
            doAcquireInterruptibly(arg);
    }

    public final boolean tryAcquireNanos(int arg, long nanosTimeout)
            throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        return tryAcquire(arg) ||
            doAcquireNanos(arg, nanosTimeout);
    }

    public final boolean release(int arg) {
        // 该方法由继承AQS的子类实现, 为释放锁的具体逻辑
        if (tryRelease(arg)) {
            Node h = head;
            // 当一个head节点的waitStatus为0，
            // 说明这个head节点后面没有在挂起等待中的后继节点了，也就不要执行 unparkSuccessor 操作了.
            // 因为如果有, head的ws就会被后继节点设为Node.SIGNAL
            if (h != null && h.waitStatus != 0)
                // 唤醒后继线程
                unparkSuccessor(h);
            return true;
        }
        return false;
    }


    // 获取共享锁
    public final void acquireShared(int arg) {
        // tryAcquireShared(int acquires)返回的是一个整型值, 只要该返回值大于等于0，就表示获取共享锁成功
        // 如果该值小于0，则代表当前线程获取共享锁失败
        // 如果该值大于0，则代表当前线程获取共享锁成功，并且接下来其他线程尝试获取共享锁的行为很可能成功
        // 如果该值等于0，则代表当前线程获取共享锁成功，但是接下来其他线程尝试获取共享锁的行为会失败
        if (tryAcquireShared(arg) < 0)
            doAcquireShared(arg);
    }


    public final void acquireSharedInterruptibly(int arg)
            throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        if (tryAcquireShared(arg) < 0)
            doAcquireSharedInterruptibly(arg);
    }


    public final boolean tryAcquireSharedNanos(int arg, long nanosTimeout)
            throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        return tryAcquireShared(arg) >= 0 ||
            doAcquireSharedNanos(arg, nanosTimeout);
    }

    public final boolean releaseShared(int arg) {
        if (tryReleaseShared(arg)) {
            // 释放共享锁
            doReleaseShared();
            return true;
        }
        return false;
    }

    public final boolean hasQueuedThreads() {
        return head != tail;
    }

    public final boolean hasContended() {
        return head != null;
    }

    public final Thread getFirstQueuedThread() {
        // handle only fast path, else relay
        return (head == tail) ? null : fullGetFirstQueuedThread();
    }


    private Thread fullGetFirstQueuedThread() {

        Node h, s;
        Thread st;
        if (((h = head) != null && (s = h.next) != null &&
             s.prev == head && (st = s.thread) != null) ||
            ((h = head) != null && (s = h.next) != null &&
             s.prev == head && (st = s.thread) != null))
            return st;

        Node t = tail;
        Thread firstThread = null;
        while (t != null && t != head) {
            Thread tt = t.thread;
            if (tt != null)
                firstThread = tt;
            t = t.prev;
        }
        return firstThread;
    }


    public final boolean isQueued(Thread thread) {
        if (thread == null)
            throw new NullPointerException();
        for (Node p = tail; p != null; p = p.prev)
            if (p.thread == thread)
                return true;
        return false;
    }

    final boolean apparentlyFirstQueuedIsExclusive() {
        Node h, s;
        return (h = head) != null &&
            (s = h.next)  != null &&
            !s.isShared()         &&
            s.thread != null;
    }


    public final boolean hasQueuedPredecessors() {
        Node t = tail;
        Node h = head;
        Node s;
        return h != t &&
            ((s = h.next) == null || s.thread != Thread.currentThread());
    }



    public final int getQueueLength() {
        int n = 0;
        for (Node p = tail; p != null; p = p.prev) {
            if (p.thread != null)
                ++n;
        }
        return n;
    }


    public final Collection<Thread> getQueuedThreads() {
        ArrayList<Thread> list = new ArrayList<Thread>();
        for (Node p = tail; p != null; p = p.prev) {
            Thread t = p.thread;
            if (t != null)
                list.add(t);
        }
        return list;
    }

    public final Collection<Thread> getExclusiveQueuedThreads() {
        ArrayList<Thread> list = new ArrayList<Thread>();
        for (Node p = tail; p != null; p = p.prev) {
            if (!p.isShared()) {
                Thread t = p.thread;
                if (t != null)
                    list.add(t);
            }
        }
        return list;
    }

    public final Collection<Thread> getSharedQueuedThreads() {
        ArrayList<Thread> list = new ArrayList<Thread>();
        for (Node p = tail; p != null; p = p.prev) {
            if (p.isShared()) {
                Thread t = p.thread;
                if (t != null)
                    list.add(t);
            }
        }
        return list;
    }

    public String toString() {
        int s = getState();
        String q  = hasQueuedThreads() ? "non" : "";
        return super.toString() +
            "[State = " + s + ", " + q + "empty queue]";
    }

    final boolean isOnSyncQueue(Node node) {
        if (node.waitStatus == Node.CONDITION || node.prev == null)
            return false;
        if (node.next != null) // If has successor, it must be on queue
            return true;

        return findNodeFromTail(node);
    }

    private boolean findNodeFromTail(Node node) {
        Node t = tail;
        for (;;) {
            if (t == node)
                return true;
            if (t == null)
                return false;
            t = t.prev;
        }
    }

    final boolean transferForSignal(Node node) {
        // 如果该节点在调用signal方法前已经被取消了，则直接跳过这个节点
        if (!compareAndSetWaitStatus(node, Node.CONDITION, 0))
            return false;
        // 如果该节点在条件队列中正常等待，则利用enq方法将该节点添加至sync queue队列的尾部
        Node p = enq(node);
        int ws = p.waitStatus;
        if (ws > 0 || !compareAndSetWaitStatus(p, ws, Node.SIGNAL))
            LockSupport.unpark(node.thread);
        return true;
    }

    final boolean transferAfterCancelledWait(Node node) {
        if (compareAndSetWaitStatus(node, Node.CONDITION, 0)) {
            enq(node);
            return true;
        }

        while (!isOnSyncQueue(node))
            Thread.yield();
        return false;
    }

    final int fullyRelease(Node node) {
        boolean failed = true;
        try {
            int savedState = getState();
            if (release(savedState)) {
                failed = false;
                return savedState;
            } else {
                throw new IllegalMonitorStateException();
            }
        } finally {
            if (failed)
                node.waitStatus = Node.CANCELLED;
        }
    }

    public final boolean owns(ConditionObject condition) {
        return condition.isOwnedBy(this);
    }

    public final boolean hasWaiters(ConditionObject condition) {
        if (!owns(condition))
            throw new IllegalArgumentException("Not owner");
        return condition.hasWaiters();
    }

    public final int getWaitQueueLength(ConditionObject condition) {
        if (!owns(condition))
            throw new IllegalArgumentException("Not owner");
        return condition.getWaitQueueLength();
    }

    public final Collection<Thread> getWaitingThreads(ConditionObject condition) {
        if (!owns(condition))
            throw new IllegalArgumentException("Not owner");
        return condition.getWaitingThreads();
    }

    /**
     * 在条件队列中，Node节点真正用到的属性只有三个：
     * thread：代表当前正在等待某个条件的线程
     * waitStatus：条件的等待状态
     * nextWaiter：指向条件队列中的下一个节点
     *
     * 只要waitStatus不是CONDITION，我们就认为线程不再等待了，此时就要从条件队列中出队
     *
     * 一般情况下，等待锁的sync queue和条件队列condition queue是相互独立的，彼此之间并没有任何关系。
     * 但是，当我们调用某个条件队列的signal方法时，会将某个或所有等待在这个条件队列中的线程唤醒，被唤醒的线程和普通线程一样需要去争锁，
     * 如果没有抢到，则同样要被加到等待锁的sync queue中去，此时节点就从condition queue中被转移到sync queue中
     *
     * condition队列是等待在特定条件下的队列，因为调用await方法时，必然是已经获得了lock锁，所以在进入condtion队列前线程必然是已经获取了锁；
     * 在被包装成Node扔进条件队列中后，线程将释放锁，然后挂起；
     * 当处于该队列中的线程被signal方法唤醒后，由于队列中的节点在之前挂起的时候已经释放了锁，所以必须先去再次的竞争锁，
     * 因此，该节点会被添加到sync queue中。因此，条件队列在出队时，线程并不持有锁。
     *
     * condition queue：入队时已经持有了锁 -> 在队列中释放锁 -> 离开队列时没有锁 -> 转移到sync queue
     * sync queue：入队时没有锁 -> 在队列中争锁 -> 离开队列时获得了锁
     */
    public class ConditionObject implements Condition, java.io.Serializable {
        private static final long serialVersionUID = 1173984872572414699L;
        // 条件队列队头
        private transient Node firstWaiter;
        // 条件队列队尾
        private transient Node lastWaiter;

        public ConditionObject() { }

        private Node addConditionWaiter() {
            // 线程在此处已经获得锁，不需要CAS
            Node t = lastWaiter;
            if (t != null && t.waitStatus != Node.CONDITION) {
                // 如果尾节点被cancel了，则先遍历整个链表，清除所有被cancel的节点
                unlinkCancelledWaiters();
                t = lastWaiter;
            }
            Node node = new Node(Thread.currentThread(), Node.CONDITION);
            if (t == null)
                firstWaiter = node;
            else
                t.nextWaiter = node;
            lastWaiter = node;
            return node;
        }

        // 唤醒线程
        private void doSignal(Node first) {
            do {
                if ( (firstWaiter = first.nextWaiter) == null)
                    lastWaiter = null;
                // 断开原来的链接（nextWaiter）
                first.nextWaiter = null;
            } while (!transferForSignal(first) &&
                     (first = firstWaiter) != null);
        }

        // 首先通过lastWaiter = firstWaiter = null;
        // 将整个条件队列清空，然后通过一个do-while循环，将原先的条件队列里面的节点一个一个拿出来(令nextWaiter = null)，
        // 再通过transferForSignal方法一个一个添加到sync queue的末尾
        private void doSignalAll(Node first) {
            lastWaiter = firstWaiter = null;
            do {
                Node next = first.nextWaiter;
                first.nextWaiter = null;
                transferForSignal(first);
                first = next;
            } while (first != null);
        }

        private void unlinkCancelledWaiters() {
            Node t = firstWaiter;
            Node trail = null;
            while (t != null) {
                Node next = t.nextWaiter;
                if (t.waitStatus != Node.CONDITION) {
                    t.nextWaiter = null;
                    if (trail == null)
                        firstWaiter = next;
                    else
                        trail.nextWaiter = next;
                    if (next == null)
                        lastWaiter = trail;
                }
                else
                    trail = t;
                t = next;
            }
        }

        public final void signal() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            Node first = firstWaiter;
            if (first != null)
                // 对于AQS的实现来说，就是唤醒条件队列中第一个没有被Cancel的节点
                doSignal(first);
        }

        public final void signalAll() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            Node first = firstWaiter;
            if (first != null)
                doSignalAll(first);
        }

        public final void awaitUninterruptibly() {
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            boolean interrupted = false;
            while (!isOnSyncQueue(node)) {
                LockSupport.park(this);
                if (Thread.interrupted())
                    interrupted = true;
            }
            if (acquireQueued(node, savedState) || interrupted)
                selfInterrupt();
        }

        private static final int REINTERRUPT =  1;

        private static final int THROW_IE    = -1;

        private int checkInterruptWhileWaiting(Node node) {
            return Thread.interrupted() ?
                (transferAfterCancelledWait(node) ? THROW_IE : REINTERRUPT) :
                0;
        }

        private void reportInterruptAfterWait(int interruptMode)
            throws InterruptedException {
            if (interruptMode == THROW_IE)
                throw new InterruptedException();
            else if (interruptMode == REINTERRUPT)
                selfInterrupt();
        }

        /**
         * 1、进入await()时必须是已经持有了锁
         * 2、离开await()时同样必须是已经持有了锁
         * 3、调用await()会使得当前线程被封装成Node扔进条件队列，然后释放所持有的锁
         * 4、释放锁后，当前线程将在condition queue中被挂起，等待signal或者中断
         * 5、线程被唤醒后会将会离开condition queue进入sync queue中进行抢锁
         * 6、若在线程抢到锁之前发生过中断，则根据中断发生在signal之前还是之后记录中断模式
         * 7、线程在抢到锁后进行善后工作（离开condition queue, 处理中断异常）
         * 8、线程已经持有了锁，从await()方法返回
         */
        public final void await() throws InterruptedException {
            // 如果当前线程在调动await()方法前已经被中断了，则直接抛出InterruptedException
            if (Thread.interrupted()) throw new InterruptedException();
            // 将当前线程封装成Node添加到条件队列   Node node = new Node(Thread.currentThread(), Node.CONDITION);
            Node node = addConditionWaiter();
            // 释放当前线程所占用的锁，保存当前的锁状态
            int savedState = fullyRelease(node);
            // 0 ： 代表整个过程中一直没有中断发生。
            // THROW_IE ： 表示退出await()方法时需要抛出InterruptedException，这种模式对应于中断发生在signal之前
            // REINTERRUPT ： 表示退出await()方法时只需要再自我中断以下，这种模式对应于中断发生在signal之后，即中断来的太晚了
            int interruptMode = 0;
            // 如果当前队列不在同步队列中，说明刚刚被await, 还没有人调用signal方法，则直接将当前线程挂起
            while (!isOnSyncQueue(node)) {
                LockSupport.park(this);
                // 能执行到这里说明要么是signal方法被调用了，要么是线程被中断了
                // 所以检查下线程被唤醒的原因，如果是因为中断被唤醒，则跳出while循环
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                    break;
            }
            // 线程将在sync queue中利用进行acquireQueued方法进行“阻塞式”争锁，抢到锁就返回，抢不到锁就继续被挂起
            // 当await()方法返回时，必然是保证了当前线程已经持有了lock锁
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
                interruptMode = REINTERRUPT;
            if (node.nextWaiter != null) // clean up if cancelled
                unlinkCancelledWaiters();
            if (interruptMode != 0)
                reportInterruptAfterWait(interruptMode);
        }

        public final long awaitNanos(long nanosTimeout)
                throws InterruptedException {
            if (Thread.interrupted())
                throw new InterruptedException();
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            final long deadline = System.nanoTime() + nanosTimeout;
            int interruptMode = 0;
            while (!isOnSyncQueue(node)) {
                if (nanosTimeout <= 0L) {
                    transferAfterCancelledWait(node);
                    break;
                }
                if (nanosTimeout >= spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanosTimeout);
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                    break;
                nanosTimeout = deadline - System.nanoTime();
            }
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
                interruptMode = REINTERRUPT;
            if (node.nextWaiter != null)
                unlinkCancelledWaiters();
            if (interruptMode != 0)
                reportInterruptAfterWait(interruptMode);
            return deadline - System.nanoTime();
        }

        public final boolean awaitUntil(Date deadline)
                throws InterruptedException {
            long abstime = deadline.getTime();
            if (Thread.interrupted())
                throw new InterruptedException();
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            boolean timedout = false;
            int interruptMode = 0;
            while (!isOnSyncQueue(node)) {
                if (System.currentTimeMillis() > abstime) {
                    timedout = transferAfterCancelledWait(node);
                    break;
                }
                LockSupport.parkUntil(this, abstime);
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                    break;
            }
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
                interruptMode = REINTERRUPT;
            if (node.nextWaiter != null)
                unlinkCancelledWaiters();
            if (interruptMode != 0)
                reportInterruptAfterWait(interruptMode);
            return !timedout;
        }

        public final boolean await(long time, TimeUnit unit)
                throws InterruptedException {
            long nanosTimeout = unit.toNanos(time);
            if (Thread.interrupted())
                throw new InterruptedException();
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            final long deadline = System.nanoTime() + nanosTimeout;
            boolean timedout = false;
            int interruptMode = 0;
            while (!isOnSyncQueue(node)) {
                if (nanosTimeout <= 0L) {
                    timedout = transferAfterCancelledWait(node);
                    break;
                }
                if (nanosTimeout >= spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanosTimeout);
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                    break;
                nanosTimeout = deadline - System.nanoTime();
            }
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
                interruptMode = REINTERRUPT;
            if (node.nextWaiter != null)
                unlinkCancelledWaiters();
            if (interruptMode != 0)
                reportInterruptAfterWait(interruptMode);
            return !timedout;
        }

        protected final boolean hasWaiters() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
                if (w.waitStatus == Node.CONDITION)
                    return true;
            }
            return false;
        }

        protected final int getWaitQueueLength() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            int n = 0;
            for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
                if (w.waitStatus == Node.CONDITION)
                    ++n;
            }
            return n;
        }

        protected final Collection<Thread> getWaitingThreads() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            ArrayList<Thread> list = new ArrayList<Thread>();
            for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
                if (w.waitStatus == Node.CONDITION) {
                    Thread t = w.thread;
                    if (t != null)
                        list.add(t);
                }
            }
            return list;
        }
    }

    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final long stateOffset;
    private static final long headOffset;
    private static final long tailOffset;
    private static final long waitStatusOffset;
    private static final long nextOffset;

    static {
        try {
            stateOffset = unsafe.objectFieldOffset
                (AbstractQueuedSynchronizer.class.getDeclaredField("state"));
            headOffset = unsafe.objectFieldOffset
                (AbstractQueuedSynchronizer.class.getDeclaredField("head"));
            tailOffset = unsafe.objectFieldOffset
                (AbstractQueuedSynchronizer.class.getDeclaredField("tail"));
            waitStatusOffset = unsafe.objectFieldOffset
                (Node.class.getDeclaredField("waitStatus"));
            nextOffset = unsafe.objectFieldOffset
                (Node.class.getDeclaredField("next"));

        } catch (Exception ex) { throw new Error(ex); }
    }

    private final boolean compareAndSetHead(Node update) {
        return unsafe.compareAndSwapObject(this, headOffset, null, update);
    }

    private final boolean compareAndSetTail(Node expect, Node update) {
        return unsafe.compareAndSwapObject(this, tailOffset, expect, update);
    }

    private static final boolean compareAndSetWaitStatus(Node node,
                                                         int expect,
                                                         int update) {
        return unsafe.compareAndSwapInt(node, waitStatusOffset,
                                        expect, update);
    }

    private static final boolean compareAndSetNext(Node node,
                                                   Node expect,
                                                   Node update) {
        return unsafe.compareAndSwapObject(node, nextOffset, expect, update);
    }
}
