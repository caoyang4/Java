
package java.util.concurrent.locks;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.LockSupport;

/**
 * 所有获取锁的方法，都返回一个邮戳（Stamp），Stamp为0表示获取失败，其余都表示成功；
 * 所有释放锁的方法，都需要一个邮戳（Stamp），这个Stamp必须是和成功获取锁时得到的Stamp一致；
 * StampedLock是基于state和队列实现，不可重入的；（若线程已经持有了写锁，还未释放，该线程再去尝试获取写锁会造成死锁）
 * StampedLock有三种访问模式：
 * ① Reading（读模式）：功能和ReentrantReadWriteLock的读锁类似
 * ② Writing（写模式）：功能和ReentrantReadWriteLock的写锁类似
 * ③ Optimistic reading（乐观读模式）：这是一种优化的读模式。
 * StampedLock支持读锁和写锁的相互转换
 *   我们知道RRW中，当线程获取到写锁后，可以降级为读锁，但是读锁是不能直接升级为写锁的。
 *   StampedLock提供了读锁和写锁相互转换的功能，使得该类支持更多的应用场景。
 * 无论写锁还是读锁，都不支持Conditon等待，不可重入
 *
 * StampedLock并未实现AQS框架，但是StampedLock的基本实现思路还是利用CLH队列进行线程的管理，通过同步状态值来表示锁的状态和类型
 * StampedLock中的连续申请的读锁节点通过cowaiter新起了一个维度链表来存放，而ReentrantReadWriteLock的读锁节点是一个一个的排列在队列中的
 */
public class StampedLock implements java.io.Serializable {

    private static final long serialVersionUID = -6001602636862214147L;

    private static final int NCPU = Runtime.getRuntime().availableProcessors();

    // 尝试获取锁时，自旋超过该值，加入等待队列
    private static final int SPINS = (NCPU > 1) ? 1 << 6 : 0;

    // 首节点自旋次数超过该值，继续阻塞
    private static final int HEAD_SPINS = (NCPU > 1) ? 1 << 10 : 0;

    private static final int MAX_HEAD_SPINS = (NCPU > 1) ? 1 << 16 : 0;

    private static final int OVERFLOW_YIELD_RATE = 7; // must be power 2 - 1

    // 用于共享锁持有计数的位数，读锁占低7位
    private static final int LG_READERS = 7;

    // Values for lock state and stamp operations
    private static final long RUNIT = 1L;
    // 写锁持有计数的掩码，第7位，不允许重入，独占锁计数最大只能为1
    private static final long WBIT  = 1L << LG_READERS;
    private static final long RBITS = WBIT - 1L;
    private static final long RFULL = RBITS - 1L;
    // 共享和独占锁持有计数的掩码，用于进行与操作判断是否持有锁
    private static final long ABITS = RBITS | WBIT;
    private static final long SBITS = ~RBITS; // note overlap with ABITS

    // state 的初始值，防止初始tryOptimisticRead的值为0
    private static final long ORIGIN = WBIT << 1;

    // 中断信号
    private static final long INTERRUPTED = 1L;

    // 节点等待状态
    private static final int WAITING   = -1;
    // 节点取消状态
    private static final int CANCELLED =  1;

    // 共享锁模式
    private static final int RMODE = 0;
    // 独占锁模式
    private static final int WMODE = 1;

    // 等待队列中的节点
    static final class WNode {
        volatile WNode prev;
        volatile WNode next;
        volatile WNode cowait;    // 读模式用该节点形成栈
        volatile Thread thread;   // non-null while possibly parked
        // 0：初始状态
        // -1：等待中
        // 1：取消
        volatile int status;      // 0, WAITING, or CANCELLED
        final int mode;           // RMODE or WMODE
        WNode(int m, WNode p) { mode = m; prev = p; }
    }

    // 等待队列头结点
    private transient volatile WNode whead;
    // 等待队列尾结点
    private transient volatile WNode wtail;

    // views
    transient ReadLockView readLockView;
    transient WriteLockView writeLockView;
    transient ReadWriteLockView readWriteLockView;

    /** Lock sequence/state */
    private transient volatile long state;
    // 读锁使用前 7 位，当超过 127 之后，使用 int 记录
    private transient int readerOverflow;

    // 初始无锁
    public StampedLock() {
        state = ORIGIN;
    }

    public long writeLock() {
        long s, next;  // bypass acquireWrite in fully unlocked case only
        return ((((s = state) & ABITS) == 0L && // 读锁或写锁未被使用
                // 将第 8 位置 1，表明占用写锁
                U.compareAndSwapLong(this, STATE, s, next = s + WBIT)) ?
                next :
                // cas 失败，调用acquireWrite，加入等待队列
                acquireWrite(false, 0L));
    }

    public long tryWriteLock() {
        // 如果获取锁成功则返回邮戳，否则返回 0
        long s, next;
        return ((((s = state) & ABITS) == 0L &&
                 U.compareAndSwapLong(this, STATE, s, next = s + WBIT)) ?
                next : 0L);
    }

    // 超时获取锁
    public long tryWriteLock(long time, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(time);
        if (!Thread.interrupted()) {
            long next, deadline;
            if ((next = tryWriteLock()) != 0L)
                return next;
            if (nanos <= 0L)
                return 0L;
            if ((deadline = System.nanoTime() + nanos) == 0L)
                deadline = 1L;
            if ((next = acquireWrite(true, deadline)) != INTERRUPTED)
                return next;
        }
        throw new InterruptedException();
    }

    // 中断式获取锁
    public long writeLockInterruptibly() throws InterruptedException {
        long next;
        if (!Thread.interrupted() &&
            (next = acquireWrite(true, 0L)) != INTERRUPTED)
            return next;
        throw new InterruptedException();
    }

    // 获取读锁，不响应中断
    public long readLock() {
        long s = state, next;  // bypass acquireRead on common uncontended case
                // 队列为空
        return ((whead == wtail &&
                // 写锁未被占用，且读锁数量未超限
                (s & ABITS) < RFULL &&
                // cas 获取锁
                 U.compareAndSwapLong(this, STATE, s, next = s + RUNIT)) ?
                next : acquireRead(false, 0L));
    }

    public long tryReadLock() {
        for (;;) {
            long s, m, next;
            // 当前持有独占锁，直接获取锁失败
            if ((m = (s = state) & ABITS) == WBIT)
                return 0L;
            // 共享锁计数未溢出，或者未持有锁
            else if (m < RFULL) {
                if (U.compareAndSwapLong(this, STATE, s, next = s + RUNIT))
                    return next;
            }
            // 共享锁计数溢出，通过tryIncReaderOverflow方法获取锁，
            // 并使用readerOverflow存储溢出的计数
            else if ((next = tryIncReaderOverflow(s)) != 0L)
                return next;
        }
    }

    /**
     *  在给定的等待时间内尝试获取共享锁，获取成功则立即返回邮戳。
     *  超过等待时间获取失败或者接收到中断信号则返回 0
     */
    public long tryReadLock(long time, TimeUnit unit)
        throws InterruptedException {
        long s, m, next, deadline;
        long nanos = unit.toNanos(time);
        if (!Thread.interrupted()) {
            if ((m = (s = state) & ABITS) != WBIT) {
                if (m < RFULL) {
                    if (U.compareAndSwapLong(this, STATE, s, next = s + RUNIT))
                        return next;
                }
                else if ((next = tryIncReaderOverflow(s)) != 0L)
                    return next;
            }
            if (nanos <= 0L)
                return 0L;
            if ((deadline = System.nanoTime() + nanos) == 0L)
                deadline = 1L;
            // acquireRead进行定时获取锁，true表示响应中断
            if ((next = acquireRead(true, deadline)) != INTERRUPTED)
                return next;
        }
        throw new InterruptedException();
    }

    // 调用后一直阻塞到获得共享锁，但是接受中断信号
    public long readLockInterruptibly() throws InterruptedException {
        long next;
        if (!Thread.interrupted() &&
            (next = acquireRead(true, 0L)) != INTERRUPTED)
            return next;
        // 如果发生中断则抛出异常
        throw new InterruptedException();
    }

    /**
     * 如果当前未持有独占锁则返回当前锁版本作为邮戳，用于在以后验证状态，
     * 如果已持有独占锁则返回0，也就表示获取乐观锁失败，否则就返回非0的邮戳，作为锁版本。
     * 只要获取或者释放了独占锁，都会导致锁版本的改变
     *
     * 如果当前没有线程持有写锁，则简单的返回一个非0的stamp版本信息，
     * 获取该stamp后在具体操作数据前还需要调用validate验证下该stamp是否已经不可用，
     * 也就是看当调用tryOptimisticRead返回stamp后，到到当前时间间是否有其他线程持有了写锁，
     * 如果有写锁，那么validate会返回0，否者就可以使用该stamp版本的锁对数据进行操作。
     * 由于tryOptimisticRead并没有使用CAS设置锁状态所以不需要显示的释放该锁。
     * 该锁的一个特点是适用于读多写少的场景，因为获取读锁只是使用与或操作进行检验，不涉及CAS操作，所以效率会高很多，
     * 但是同时由于没有使用真正的锁，在保证数据一致性上需要拷贝一份要操作的变量到方法栈，并且在操作数据时候可能其他写线程已经修改了数据，
     * 而我们操作的是方法栈里面的数据，也就是一个快照，所以最多返回的不是最新的数据，但是一致性还是得到保障的。
     */
    public long tryOptimisticRead() {
        long s;
        return (((s = state) & WBIT) == 0L) ? (s & SBITS) : 0L;
    }

    public boolean validate(long stamp) {
        // 定义内存屏障，避免代码重排序
        U.loadFence();
        // 比较邮储与state的锁版本是否相同
        return (stamp & SBITS) == (state & SBITS);
    }

    /**
     * 释放写锁
     * 释放锁前先判断传入的邮戳是否与当前锁状态相等，且当前为持有写锁的状态。
     * 通过锁状态增加 WBIT 使表示独占锁持有锁的位(第8位)进位到锁版本位，如果 state 值溢出则设置为 ORIGIN。
     * state 修改完成表示独占锁已经释放，如果同步队列已经初始化，并且 head 节点的状态不为0，则唤醒下一个节点
     */
    public void unlockWrite(long stamp) {
        WNode h;
        // 确定传入的锁状态是正确的，且当前持有独占锁
        if (state != stamp || (stamp & WBIT) == 0L)
            throw new IllegalMonitorStateException();
        // 增加锁版本的值，如果位溢出则重置为ORIGIN
        state = (stamp += WBIT) == 0L ? ORIGIN : stamp;
        if ((h = whead) != null && h.status != 0)
            // 唤醒它的下一个节点
            release(h);
    }

    public void unlockRead(long stamp) {
        long s, m; WNode h;
        for (;;) {
            // 传入邮戳和当前state的版本不一致
            if (((s = state) & SBITS) != (stamp & SBITS) ||
                (stamp & ABITS) == 0L || (m = s & ABITS) == 0L || m == WBIT)
                throw new IllegalMonitorStateException();
            // 共享锁状态未溢出
            if (m < RFULL) {
                if (U.compareAndSwapLong(this, STATE, s, s - RUNIT)) {
                    // 如果当前释放的是最后一个共享锁，且whead节点不为空，状态不为0
                    if (m == RUNIT && (h = whead) != null && h.status != 0)
                        // 唤醒它的下一个节点
                        release(h);
                    break;
                }
            }
            // 共享锁计数溢出，通过tryDecReaderOverflow方法释放
            else if (tryDecReaderOverflow(s) != 0L)
                break;
        }
    }

    /**
     * 如果锁状态与给定的邮戳匹配，则释放锁的相应模式
     * 如果当前持有的是共享锁，存在 CAS 失败的可能，所以需要循环进行锁释放，
     * 除非当前锁版本和邮戳的锁版本不同，或者当前未持有锁。
     * 检查通过先判断当前 state 持有的是否是独占锁，如果持有独占锁则直接修改 state 进行锁释放，
     * 否则再次对传入的邮戳进行检查。
     * 释放共享锁时区分溢出和未溢出，如果未溢出则通过 CAS 进行锁释放，如果溢出了通过 tryDecReaderOverflow 方法进行锁释放。
     */
    public void unlock(long stamp) {
        long a = stamp & ABITS, m, s; WNode h;
        // // 当前锁版本和邮戳锁版本相同
        while (((s = state) & SBITS) == (stamp & SBITS)) {
            if ((m = s & ABITS) == 0L)
                // 当前未持有锁
                break;
            // 当前持有独占锁，进行独占锁释放
            else if (m == WBIT) {
                if (a != m)
                    break;
                // 直接修改state，如果溢出则重置为ORIGIN
                // 只有一个线程可以持有独占锁，所以直接修改时线程安全的
                state = (s += WBIT) == 0L ? ORIGIN : s;
                // whead节点不为空，状态不为0
                if ((h = whead) != null && h.status != 0)
                    // 唤醒whead后续的节点
                    release(h);
                return;
            }
            // 传入邮戳错误，为未持有锁或者同时持有共享锁和独占锁的状态
            else if (a == 0L || a >= WBIT)
                break;
            else if (m < RFULL) {
                // CAS修改state释放读锁
                if (U.compareAndSwapLong(this, STATE, s, s - RUNIT)) {
                    // 如果当前释放的是最后一个共享锁，且whead节点不为空，状态不为0
                    if (m == RUNIT && (h = whead) != null && h.status != 0)
                        release(h);
                    return;
                }
            }
            // 排除以上情况，剩余情况是当前持有共享锁但是共享锁计数已经溢出
            else if (tryDecReaderOverflow(s) != 0L)
                return;
        }
        throw new IllegalMonitorStateException();
    }

    /**
     * 锁升级
     * 验证当前锁版本和锁持有状态和给定的邮戳是否匹配，
     * 如果不匹配、邮戳的锁状态有误或当前持有多个共享锁则返回 0。
     * 匹配时则分三种情况，
     *   当前未持有锁则获取独占锁，
     *   当前持有独占锁则不进行操作，
     *   当前仅持有一个共享锁则释放共享锁获取独占锁，最终返回独占锁的邮戳
     */
    public long tryConvertToWriteLock(long stamp) {
        long a = stamp & ABITS, m, s, next;
        // 当前锁版本和邮戳锁版本相同
        while (((s = state) & SBITS) == (stamp & SBITS)) {
            // 当前未持有锁
            if ((m = s & ABITS) == 0L) {
                // 邮戳为持有锁的状态
                if (a != 0L)
                    break;
                // CAS修改state获取独占锁
                if (U.compareAndSwapLong(this, STATE, s, next = s + WBIT))
                    return next;
            }
            // 当前已经持有独占锁
            else if (m == WBIT) {
                // 传入邮戳的锁状态有错
                if (a != m)
                    break;
                // 直接返回邮戳
                return stamp;
            }
            // 当前仅持有一个共享锁
            else if (m == RUNIT && a != 0L) {
                // CAS修改state释放共享锁获取独占锁
                if (U.compareAndSwapLong(this, STATE, s, next = s - RUNIT + WBIT))
                    return next;
            }
            else
                break;
        }
        return 0L;
    }

    /**
     * 锁降级
     * 验证当前锁版本和锁持有状态和给定的邮戳是否匹配，如果不匹配或者邮戳的锁状态有误则返回 0。
     * 匹配时则分三种情况，
     *   当前未持有锁则获取共享锁，
     *   当前持有独占锁则释放独占锁获取共享锁，
     *   当前持有共享锁则不进行操作，最终返回共享锁的邮戳。
     */
    public long tryConvertToReadLock(long stamp) {
        long a = stamp & ABITS, m, s, next; WNode h;
        // 当前锁版本和邮戳锁版本相同
        while (((s = state) & SBITS) == (stamp & SBITS)) {
            // 当前未持有锁
            if ((m = s & ABITS) == 0L) {
                if (a != 0L)
                    break;
                // 当前state表示共享锁计数未溢出，或者未持有锁，CAS修改state获取锁
                else if (m < RFULL) {
                    if (U.compareAndSwapLong(this, STATE, s, next = s + RUNIT))
                        return next;
                }
                // 当前共享锁计数溢出，通过tryIncReaderOverflow获取锁
                else if ((next = tryIncReaderOverflow(s)) != 0L)
                    return next;
            }
            // 当前持有独占锁
            else if (m == WBIT) {
                if (a != m)
                    break;
                // 持有独占锁只有一个线程，直接修改state释放独占锁获取共享锁
                state = next = s + (WBIT + RUNIT);
                // 独占锁被释放，如果有排队线程则进行唤醒
                if ((h = whead) != null && h.status != 0)
                    release(h);
                return next;
            }
            // 当前邮戳为持有共享锁的邮戳，不做操作直接返回邮戳
            else if (a != 0L && a < WBIT)
                return stamp;
            else
                break;
        }
        return 0L;
    }

    /**
     *  验证当前锁版本和锁持有状态和给定的邮戳是否匹配，如果匹配则进行一次锁释放，如果不匹配或者邮戳的锁状态有误则返回 0。
     *  该方法的逻辑和 unlock 方法的逻辑相似，如果当前未持有锁就直接返回锁版本，如果持有锁则进行一次锁释放，再返回锁版本。
     *  其实这个接口基本上等于，unlock + tryOptimisticRead 两个方法结合
     */
    public long tryConvertToOptimisticRead(long stamp) {
        long a = stamp & ABITS, m, s, next; WNode h;
        // 定义内存屏障，避免代码重排序
        U.loadFence();
        for (;;) {
            if (((s = state) & SBITS) != (stamp & SBITS))
                break;
            if ((m = s & ABITS) == 0L) {
                if (a != 0L)
                    break;
                return s;
            }
            else if (m == WBIT) {
                if (a != m)
                    break;
                state = next = (s += WBIT) == 0L ? ORIGIN : s;
                if ((h = whead) != null && h.status != 0)
                    release(h);
                return next;
            }
            else if (a == 0L || a >= WBIT)
                break;
            else if (m < RFULL) {
                if (U.compareAndSwapLong(this, STATE, s, next = s - RUNIT)) {
                    if (m == RUNIT && (h = whead) != null && h.status != 0)
                        release(h);
                    return next & SBITS;
                }
            }
            else if ((next = tryDecReaderOverflow(s)) != 0L)
                return next & SBITS;
        }
        return 0L;
    }

    /**
     * 如果持有独占锁则释放锁，不需要邮戳
     * 此方法对于错误后的恢复可能很有用
     * 相比于 unlockWrite 少了检查邮戳是否正确的步骤，其他流程相同
     */
    public boolean tryUnlockWrite() {
        long s; WNode h;
        if (((s = state) & WBIT) != 0L) {
            state = (s += WBIT) == 0L ? ORIGIN : s;
            if ((h = whead) != null && h.status != 0)
                release(h);
            return true;
        }
        return false;
    }

    /**
     * 如果持有共享锁则进行一次锁释放，不需要邮戳
     */
    public boolean tryUnlockRead() {
        long s, m; WNode h;
        while ((m = (s = state) & ABITS) != 0L && m < WBIT) {
            if (m < RFULL) {
                if (U.compareAndSwapLong(this, STATE, s, s - RUNIT)) {
                    if (m == RUNIT && (h = whead) != null && h.status != 0)
                        release(h);
                    return true;
                }
            }
            else if (tryDecReaderOverflow(s) != 0L)
                return true;
        }
        return false;
    }

    // 查询当前持有的共享锁计数
    private int getReadLockCount(long s) {
        long readers;
        // 共享锁计数大于或等于最大读计数RFULL
        if ((readers = s & RBITS) >= RFULL)
            // 读计数加上溢出的值
            readers = RFULL + readerOverflow;
        return (int) readers;
    }

    public boolean isWriteLocked() {
        return (state & WBIT) != 0L;
    }

    public boolean isReadLocked() {
        return (state & RBITS) != 0L;
    }

    public int getReadLockCount() {
        return getReadLockCount(state);
    }

    public String toString() {
        long s = state;
        return super.toString() + ((s & ABITS) == 0L ? "[Unlocked]" : (s & WBIT) != 0L ? "[Write-locked]" : "[Read-locks:" + getReadLockCount(s) + "]");
    }

    // views

    public Lock asReadLock() {
        ReadLockView v;
        return ((v = readLockView) != null ? v : (readLockView = new ReadLockView()));
    }

    public Lock asWriteLock() {
        WriteLockView v;
        return ((v = writeLockView) != null ? v : (writeLockView = new WriteLockView()));
    }

    public ReadWriteLock asReadWriteLock() {
        ReadWriteLockView v;
        return ((v = readWriteLockView) != null ? v : (readWriteLockView = new ReadWriteLockView()));
    }

    // view classes

    final class ReadLockView implements Lock {
        public void lock() { readLock(); }
        public void lockInterruptibly() throws InterruptedException {
            readLockInterruptibly();
        }
        public boolean tryLock() { return tryReadLock() != 0L; }
        public boolean tryLock(long time, TimeUnit unit)
            throws InterruptedException {
            return tryReadLock(time, unit) != 0L;
        }
        public void unlock() { unstampedUnlockRead(); }
        // ReadLockView 不支持 Condition
        public Condition newCondition() {
            throw new UnsupportedOperationException();
        }
    }

    final class WriteLockView implements Lock {
        public void lock() { writeLock(); }
        public void lockInterruptibly() throws InterruptedException {
            writeLockInterruptibly();
        }
        public boolean tryLock() { return tryWriteLock() != 0L; }
        public boolean tryLock(long time, TimeUnit unit)
            throws InterruptedException {
            return tryWriteLock(time, unit) != 0L;
        }
        public void unlock() { unstampedUnlockWrite(); }
        // WriteLockView 不支持 Condition
        public Condition newCondition() {
            throw new UnsupportedOperationException();
        }
    }

    final class ReadWriteLockView implements ReadWriteLock {
        public Lock readLock() { return asReadLock(); }
        public Lock writeLock() { return asWriteLock(); }
    }

    // Unlock methods without stamp argument checks for view classes.
    // Needed because view-class lock methods throw away stamps.

    final void unstampedUnlockWrite() {
        WNode h; long s;
        if (((s = state) & WBIT) == 0L)
            throw new IllegalMonitorStateException();
        state = (s += WBIT) == 0L ? ORIGIN : s;
        if ((h = whead) != null && h.status != 0)
            release(h);
    }

    final void unstampedUnlockRead() {
        for (;;) {
            long s, m; WNode h;
            if ((m = (s = state) & ABITS) == 0L || m >= WBIT)
                throw new IllegalMonitorStateException();
            else if (m < RFULL) {
                if (U.compareAndSwapLong(this, STATE, s, s - RUNIT)) {
                    if (m == RUNIT && (h = whead) != null && h.status != 0)
                        release(h);
                    break;
                }
            }
            else if (tryDecReaderOverflow(s) != 0L)
                break;
        }
    }

    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        state = ORIGIN; // reset to unlocked state
    }

    // internals

    private long tryIncReaderOverflow(long s) {
        // 确定当前state的共享锁计数已经饱和
        if ((s & ABITS) == RFULL) {
            if (U.compareAndSwapLong(this, STATE, s, s | RBITS)) {
                // 修改 state 成功表示获取锁成功，线程安全
                ++readerOverflow;
                // 修改锁状态
                state = s;
                return s;
            }
        }
        else if ((LockSupport.nextSecondarySeed() & OVERFLOW_YIELD_RATE) == 0)
            Thread.yield();
        return 0L;
    }

    private long tryDecReaderOverflow(long s) {
        // assert (s & ABITS) >= RFULL;
        if ((s & ABITS) == RFULL) {
            if (U.compareAndSwapLong(this, STATE, s, s | RBITS)) {
                int r; long next;
                if ((r = readerOverflow) > 0) {
                    readerOverflow = r - 1;
                    next = s;
                }
                else
                    next = s - RUNIT;
                 state = next;
                 return next;
            }
        }
        else if ((LockSupport.nextSecondarySeed() &
                  OVERFLOW_YIELD_RATE) == 0)
            Thread.yield();
        return 0L;
    }

    /**
     * release 方法用于唤醒传入节点的后继节点。
     * 方法传入一个节点 h，如果传入节点的状态为 WAITING 首先修改传入节点的 state 状态为 0，
     * 如果传入节点的后继节点为空或者被取消，从后往前遍历找到该节点后继第一个未被取消的节点 q，如果 q 在阻塞状态则进行唤醒
     */

    private void release(WNode h) {
        if (h != null) {
            WNode q; Thread w;
            // 取消等待状态
            U.compareAndSwapInt(h, WSTATUS, WAITING, 0);
            // 传入节点不存在后继节点，或后继节点被取消
            if ((q = h.next) == null || q.status == CANCELLED) {
                // 从后往前查找到离h最近的一个未被取消的节点
                for (WNode t = wtail; t != null && t != h; t = t.prev)
                    if (t.status <= 0)
                        q = t;
            }
            // 后继节点存在且在阻塞中
            if (q != null && (w = q.thread) != null)
                // 唤醒后继节点
                U.unpark(w);
        }
    }

    private long acquireWrite(boolean interruptible, long deadline) {
        // node为新增节点，p为尾节点（将成为node的前置节点）
        WNode node = null, p;
        for (int spins = -1;;) {
            long m, s, ns;
            // 低8为为0，表示还未持有锁，直接CAS修改state获取锁
            if ((m = (s = state) & ABITS) == 0L) {
                if (U.compareAndSwapLong(this, STATE, s, ns = s + WBIT))
                    return ns;
            }
            else if (spins < 0)
                // 当前持有的是写锁，且wtail和whead为同一个节点，则自旋SPINS次获取锁
                spins = (m == WBIT && wtail == whead) ? SPINS : 0;
            else if (spins > 0) {
                // 自旋计数-1
                if (LockSupport.nextSecondarySeed() >= 0)
                    --spins;
            }
            // wtail为null表示队列还未初始化，进行初始化队列
            else if ((p = wtail) == null) { // initialize queue
                WNode hd = new WNode(WMODE, null);
                if (U.compareAndSwapObject(this, WHEAD, null, hd))
                    // 将 hd 节点cas设置为头节点，成功则让队尾指向该节点
                    wtail = hd;
            }
            // 创建当前线程的node节点
            else if (node == null)
                node = new WNode(WMODE, p);
            // 如果队尾节点有变动，当前线程节点重新指定前驱节点，即最新的尾结点
            else if (node.prev != p)
                node.prev = p;
            // cas将当前线程的node节点设置为wtail，成功则将队尾的下一节点指向当前节点，并跳出自旋
            else if (U.compareAndSwapObject(this, WTAIL, p, node)) {
                p.next = node;
                break;
            }
        }
        // 阻塞当前线程进行排队
        for (int spins = -1;;) {
            // h为头节点，np为新增节点的前置节点，pp为前前置节点，ps为前置节点的状态
            WNode h, np, pp; int ps;
            if ((h = whead) == p) {
                // 设置自旋的值
                if (spins < 0)
                    spins = HEAD_SPINS;
                // 自旋次数小于MAX_HEAD_SPINS时自旋次数x2
                else if (spins < MAX_HEAD_SPINS)
                    spins <<= 1;

                // 自旋获取锁
                for (int k = spins;;) { // spin at head
                    long s, ns;
                    // 当前未获取锁
                    if (((s = state) & ABITS) == 0L) {
                        // CAS修改state，修改成功表示获取锁成功
                        if (U.compareAndSwapLong(this, STATE, s, ns = s + WBIT)) {
                            // 获取锁成功，将node设置为whead
                            whead = node;
                            node.prev = null;
                            return ns;
                        }
                    }
                    // 自旋次数自减
                    else if (LockSupport.nextSecondarySeed() >= 0 && --k <= 0)
                        break;
                }
            }
            // 队首和队尾不是同一节点，队列非空
            else if (h != null) { // help release stale waiters
                WNode c; Thread w;
                // 如果队首节点的 cowait 字段非空说明是读节点，唤醒队列 cowait 中的所有节点
                while ((c = h.cowait) != null) {
                    if (U.compareAndSwapObject(h, WCOWAIT, c, c.cowait) && (w = c.thread) != null)
                        U.unpark(w);
                }
            }
            // 头结点未变的情况
            if (whead == h) {
                // 前驱节点改变，为前驱节点重新指定next为当前节点
                if ((np = node.prev) != p) {
                    if (np != null)
                        (p = np).next = node;
                }
                // 如果当前节点的前驱节点状态为0，将其前驱节点设置为等待状态
                else if ((ps = p.status) == 0)
                    U.compareAndSwapInt(p, WSTATUS, 0, WAITING);
                //如果当前节点的前驱节点状态为取消
                else if (ps == CANCELLED) {
                    // 重新设置前驱节点，移除原前驱节点
                    if ((pp = p.prev) != null) {
                        node.prev = pp;
                        pp.next = node;
                    }
                }
                else {
                    long time;
                    // 传入0，表示阻塞直到UnSafe.unpark唤醒
                    if (deadline == 0L)
                        time = 0L;
                    // 等待超时，取消当前等待节点
                    else if ((time = deadline - System.nanoTime()) <= 0L)
                        return cancelWaiter(node, node, false);
                    Thread wt = Thread.currentThread();
                    // 设置线程Thread的parkblocker属性，表示当前线程被谁阻塞，用于监控线程使用
                    U.putObject(wt, PARKBLOCKER, this);
                    // 将其当前线程设置为当前节点
                    node.thread = wt;
                    // 当前节点的前驱节点为等待状态，并且队列的头节点不是前驱节点或者当前状态为有锁状态
                    if (p.status < 0 && (p != h || (state & ABITS) != 0L) &&
                        // 队列头节点和当前节点的前驱节点未改变，则阻塞当前线程
                        whead == h && node.prev == p)
                        U.park(false, time);
                    // 将当前node的线程设置为null
                    node.thread = null;
                    // 当前线程的监控对象也置为空
                    U.putObject(wt, PARKBLOCKER, null);
                    //如果传入的参数interruptible为true，并且当前线程中断，取消当前节点
                    if (interruptible && Thread.interrupted())
                        return cancelWaiter(node, node, true);
                }
            }
        }
    }

    /**
     * 尝试自旋的获取读锁, 获取不到则加入等待队列, 并阻塞线程
     *
     * @param interruptible true 表示检测中断, 如果线程被中断过, 则最终返回INTERRUPTED
     * @param deadline      如果非0, 则表示限时获取
     * @return 非0表示获取成功, INTERRUPTED表示中途被中断过
     */
    private long acquireRead(boolean interruptible, long deadline) {
        // node指向入队结点, p指向入队前的队尾结点
        WNode node = null, p;
        for (int spins = -1;;) {
            WNode h;
            // 如果队列为空或只有头结点, 则会立即尝试获取读锁
            if ((h = whead) == (p = wtail)) {
                for (long m, s, ns;;) {
                    // 当前持有读锁，并且持有读锁计数没有溢出，或者未持有锁
                    if ((m = (s = state) & ABITS) < RFULL ? // 如果持有写锁，由于写锁在高位，必定大于RFULL
                        // cas更新同步状态
                        U.compareAndSwapLong(this, STATE, s, ns = s + RUNIT) :
                        // 读锁数量超限, 超出部分放到readerOverflow字段中
                        (m < WBIT && (ns = tryIncReaderOverflow(s)) != 0L))
                        return ns;
                    // 写锁被占用，计数自减操作
                    else if (m >= WBIT) {
                        if (spins > 0) {
                            if (LockSupport.nextSecondarySeed() >= 0)
                                --spins;
                        }
                        else {
                            // 自旋结束，判断头尾节点是否相等
                            if (spins == 0) {
                                WNode nh = whead, np = wtail;
                                // 如果头尾节点没有改变或者头尾节点不相等，退出自旋
                                if ((nh == h && np == p) || (h = nh) != (p = np))
                                    break;
                            }
                            // 继续自旋
                            spins = SPINS;
                        }
                    }
                }
            }
            // 执行到此处，说明要加入等待队列了
            // p == null表示队列为空, 则初始化队列(构造头结点)
            if (p == null) {
                WNode hd = new WNode(WMODE, null);
                // head只是一个哨兵节点，thread 为 null，相当于 CLH 的 dummy
                if (U.compareAndSwapObject(this, WHEAD, null, hd))
                    wtail = hd;
            }
            else if (node == null)
                // 将当前线程包装成读结点
                node = new WNode(RMODE, p);
            else if (h == p || p.mode != RMODE) {
                // 如果当前节点的前驱节点不是尾节点，重新设置当前节点的前驱节点
                if (node.prev != p)
                    node.prev = p;
                // 将当前节点加入队列中，并且当前节点做为尾节点，如果成功退出循环
                else if (U.compareAndSwapObject(this, WTAIL, p, node)) {
                    p.next = node;
                    break;
                }
            }
            // 将当前节点加入尾节点的cowait栈
            else if (!U.compareAndSwapObject(p, WCOWAIT, node.cowait = p.cowait, node))
                node.cowait = null;
            else {
                for (;;) {
                    WNode pp, c; Thread w;
                    // 如果头节点的cowait栈不为空，并且其线程不为null，将cowait栈第一个节点出栈并唤醒
                    if ((h = whead) != null && (c = h.cowait) != null &&
                        U.compareAndSwapObject(h, WCOWAIT, c, c.cowait) &&
                        (w = c.thread) != null) // help release
                        U.unpark(w);
                    // 如果当前头节点为尾节点的前驱节点，或者头尾相同，或者尾节点前驱节点为空
                    if (h == (pp = p.prev) || h == p || pp == null) {
                        long m, s, ns;
                        do {
                            // 判断当前state是否未持有锁，或处于共享锁状态，且没有溢出
                            if ((m = (s = state) & ABITS) < RFULL ?
                                // CAS修改state获取锁
                                U.compareAndSwapLong(this, STATE, s, ns = s + RUNIT) :
                                // 当前持有共享锁，并且持有计数溢出，通过tryIncReaderOverflow获取锁，成功则 return
                                (m < WBIT && (ns = tryIncReaderOverflow(s)) != 0L))
                                return ns;
                        } while (m < WBIT);  // 当前未持有独占锁，进入循环
                    }
                    // 如果头结点没有改变，并且尾节点的前驱节点没有改变
                    if (whead == h && p.prev == pp) {
                        long time;
                        // 如果前前节点为空，或者头节点为前置节点，或者前置节点已经取消
                        // 外部循环重新开始创建node节点
                        if (pp == null || h == p || p.status > 0) {
                            node = null; // throw away
                            break;
                        }
                        if (deadline == 0L)
                            time = 0L;
                        else if ((time = deadline - System.nanoTime()) <= 0L)
                            // 已经超时，取消当前节点
                            return cancelWaiter(node, p, false);
                        Thread wt = Thread.currentThread();
                        // 设置线程Thread的parkblocker属性，表示当前线程被谁阻塞，用于监控线程使用
                        U.putObject(wt, PARKBLOCKER, this);
                        // 将其当前线程设置为当前节点
                        node.thread = wt;
                        if ((h != pp || (state & ABITS) == WBIT) &&
                            whead == h && p.prev == pp)
                            // 阻塞当前线程
                            U.park(false, time);
                        // 执行到此处，说明被唤醒或中断，当前节点的线程置空，退出队列，相当于出队
                        node.thread = null;
                        // 当前线程的监控对象也置为空
                        U.putObject(wt, PARKBLOCKER, null);
                        if (interruptible && Thread.interrupted())
                            // 如果interruptible为true，线程中断则取消当前节点
                            return cancelWaiter(node, p, true);
                    }
                }
            }
        }
        // 阻塞当前线程，在阻塞当前线程之前，如果头节点和尾节点相等，让其自旋一段时间获取写锁。
        // 如果头结点不为空，释放头节点的cowait队列
        for (int spins = -1;;) {
            WNode h, np, pp; int ps;
            // 如果头节点和尾节点相等
            if ((h = whead) == p) {
                // 设置自旋的初始值
                if (spins < 0)
                    spins = HEAD_SPINS;
                // 每次进入自旋，自旋次数*2，直到大于等于MAX_HEAD_SPINS
                else if (spins < MAX_HEAD_SPINS)
                    spins <<= 1;
                for (int k = spins;;) { // spin at head
                    long m, s, ns;
                    // 当前未持有锁或者持有共享锁且持有计数未溢出
                    if ((m = (s = state) & ABITS) < RFULL ?
                        U.compareAndSwapLong(this, STATE, s, ns = s + RUNIT) :
                        (m < WBIT && (ns = tryIncReaderOverflow(s)) != 0L)) {
                        WNode c; Thread w;
                        // 将当前节点设置为whead
                        whead = node;
                        // 前置节点设置为空
                        node.prev = null;
                        // 唤醒node节点的cowait栈
                        while ((c = node.cowait) != null) {
                            if (U.compareAndSwapObject(node, WCOWAIT, c, c.cowait) && (w = c.thread) != null)
                                U.unpark(w);
                        }
                        return ns;
                    }
                    // 当前持有写锁，自旋次数-1
                    else if (m >= WBIT && LockSupport.nextSecondarySeed() >= 0 && --k <= 0)
                        break;
                }
            }
            // 如果whead不为空，唤醒node节点的cowait栈
            else if (h != null) {
                WNode c; Thread w;
                while ((c = h.cowait) != null) {
                    if (U.compareAndSwapObject(h, WCOWAIT, c, c.cowait) && (w = c.thread) != null)
                        U.unpark(w);
                }
            }
            // 如果头结点没有改变
            if (whead == h) {
                // 当前节点的前置节点改变，更新前置节点的next
                if ((np = node.prev) != p) {
                    if (np != null)
                        (p = np).next = node;   // stale
                }
                // 当前节点的前置节点状态为0，更新为WAITING
                else if ((ps = p.status) == 0)
                    U.compareAndSwapInt(p, WSTATUS, 0, WAITING);
                // 当前节点的前置节点已被取消，从同步队列中删除
                else if (ps == CANCELLED) {
                    if ((pp = p.prev) != null) {
                        node.prev = pp;
                        pp.next = node;
                    }
                }
                else {
                    long time;
                    if (deadline == 0L)
                        time = 0L;
                    else if ((time = deadline - System.nanoTime()) <= 0L)
                        return cancelWaiter(node, node, false);
                    Thread wt = Thread.currentThread();
                    U.putObject(wt, PARKBLOCKER, this);
                    node.thread = wt;
                    // 如果当前节点的前驱节点为等待状态
                    // 并且头尾节点不相等或者当前状态为独占锁状态
                    // 并且头结点不变，当前节点的前驱节点不变
                    if (p.status < 0 && (p != h || (state & ABITS) == WBIT) && whead == h && node.prev == p)
                        U.park(false, time);
                    node.thread = null;
                    U.putObject(wt, PARKBLOCKER, null);
                    if (interruptible && Thread.interrupted())
                        return cancelWaiter(node, node, true);
                }
            }
        }
    }

    private long cancelWaiter(WNode node, WNode group, boolean interrupted) {
        if (node != null && group != null) {
            Thread w;
            // 将 node 节点状态设置为取消状态，1
            node.status = CANCELLED;
            // 遍历group的cowait栈，清除CANCELLED节点
            for (WNode p = group, q; (q = p.cowait) != null;) {
                if (q.status == CANCELLED) {
                    U.compareAndSwapObject(p, WCOWAIT, q, q.cowait);
                    p = group; // restart
                }
                else
                    p = q;
            }
            // node和group相同
            if (group == node) {
                // 唤醒group的cowait栈
                for (WNode r = group.cowait; r != null; r = r.cowait) {
                    if ((w = r.thread) != null)
                        U.unpark(w);       // wake up uncancelled co-waiters
                }
                // 将当前取消接节点的前驱节点的下一个节点设置为当前取消节点的next节点
                for (WNode pred = node.prev; pred != null; ) { // unsplice
                    WNode succ, pp;        // find valid successor
                    // 如果当前取消节点的下一个节点为空或者是取消状态，从尾节点开始寻找有效的节点
                    // 并重新指定下一个节点
                    while ((succ = node.next) == null ||
                           succ.status == CANCELLED) {
                        WNode q = null;    // find successor the slow way
                        // 从wtail向前遍历到node，找到离node最近一个未被取消的节点
                        for (WNode t = wtail; t != null && t != node; t = t.prev)
                            if (t.status != CANCELLED)
                                q = t;     // don't link if succ cancelled
                        if (succ == q ||   // ensure accurate successor
                            // 修改node的后继节点为最近一个未被取消的节点
                            U.compareAndSwapObject(node, WNEXT, succ, succ = q)) {
                            // 当前节点后继节点为空，且当前节点为wtail节点
                            if (succ == null && node == wtail)
                                U.compareAndSwapObject(this, WTAIL, node, pred);
                            break;
                        }
                    }
                    if (pred.next == node) // unsplice pred link
                        // 修改node前置节点的next为node后最近一个未被取消的节点
                        U.compareAndSwapObject(pred, WNEXT, node, succ);
                    // 后继节点存在，且在线程阻塞状态
                    if (succ != null && (w = succ.thread) != null) {
                        succ.thread = null;
                        // 唤醒后继节点
                        U.unpark(w);       // wake up succ to observe new pred
                    }
                    // 当前节点的前驱节点未被取消，或者前驱接点的前驱节点为空，退出循环
                    if (pred.status != CANCELLED || (pp = pred.prev) == null)
                        break;
                    // 重新设置当前取消节点的前驱节点
                    node.prev = pp;        // repeat if new pred wrong/cancelled
                    U.compareAndSwapObject(pp, WNEXT, pred, succ);
                    // 将其前驱节点设置为pp，重新循环
                    pred = pp;
                }
            }
        }
        WNode h; // Possibly release first waiter
        while ((h = whead) != null) {
            long s; WNode q; // similar to release() but check eligibility
            // 头节点的下一节点为空或者是取消状态，从尾节点开始寻找有效的节点（包括等待状态，和运行状态）
            if ((q = h.next) == null || q.status == CANCELLED) {
                for (WNode t = wtail; t != null && t != h; t = t.prev)
                    if (t.status <= 0)
                        q = t;
            }
            // 如果头节点没有改变
            if (h == whead) {
                // 头节点的下一有效节点不为空，并且头节点的状态为0
                if (q != null && h.status == 0 &&
                    // 当前StampedLock的不为写锁状态
                    ((s = state) & ABITS) != WBIT && // waiter is eligible
                    // 头节点的下一节点为读模式，唤醒头结点的下一节点
                    (s == 0L || q.mode == RMODE))
                    // 唤醒头结点的下一有效节点
                    release(h);
                break;
            }
        }
        //如果当前线程被中断或者传入进来的interrupted为true，直接返回中断标志位，否则返回0
        return (interrupted || Thread.interrupted()) ? INTERRUPTED : 0L;
    }

    // Unsafe mechanics
    private static final sun.misc.Unsafe U;
    private static final long STATE;
    private static final long WHEAD;
    private static final long WTAIL;
    private static final long WNEXT;
    private static final long WSTATUS;
    private static final long WCOWAIT;
    private static final long PARKBLOCKER;

    static {
        try {
            U = sun.misc.Unsafe.getUnsafe();
            Class<?> k = StampedLock.class;
            Class<?> wk = WNode.class;
            STATE = U.objectFieldOffset
                (k.getDeclaredField("state"));
            WHEAD = U.objectFieldOffset
                (k.getDeclaredField("whead"));
            WTAIL = U.objectFieldOffset
                (k.getDeclaredField("wtail"));
            WSTATUS = U.objectFieldOffset
                (wk.getDeclaredField("status"));
            WNEXT = U.objectFieldOffset
                (wk.getDeclaredField("next"));
            WCOWAIT = U.objectFieldOffset
                (wk.getDeclaredField("cowait"));
            Class<?> tk = Thread.class;
            PARKBLOCKER = U.objectFieldOffset
                (tk.getDeclaredField("parkBlocker"));

        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
