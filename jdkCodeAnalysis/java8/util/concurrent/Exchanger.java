package java.util.concurrent;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

/**
 * Exchanger 是 Java 并发包中的一个工具类，它可以实现两个线程之间的数据交换。
 * 当一个线程执行到 Exchanger 的 exchange() 方法时，它会将自己的数据传递给另一个线程，并等待对方的数据。
 * 当两个线程都执行到 exchange() 方法时，它们会进行数据交换。
 *
 * Exchanger 的应用场景如下：
 * 1. 两线程之间需要一次性交换大量数据时；
 * 2. 两线程之间需要相互协作完成某项任务时；
 * 3. 异步数据流中两个线程之间需要交换数据，一个线程扮演生产者角色，另一个线程扮演消费者角色。
 *
 * 需要注意的是，Exchanger 可以实现一对一的数据交换，如果有多个线程需要交换数据，需要使用多个 Exchanger 来协调。
 * 同时，由于 Exchanger 是一种同步机制，如果某个线程在交换数据时发生异常，可能会出现死锁等情况，需要谨慎使用。
 */
public class Exchanger<V> {

    /**
     * Overview: The core algorithm is, for an exchange "slot", and a participant (caller) with an item:
     *
     * for (;;) {
     *   if (slot is empty) {                       // offer
     *     place item in a Node;
     *     if (can CAS slot from empty to node) {
     *       wait for release;
     *       return matching item in node;
     *     }
     *   }
     *   else if (can CAS slot from node to empty) { // release
     *     get the item in node;
     *     set matching item in node;
     *     release waiting thread;
     *   }
     *   // else retry on CAS failure
     * }
     *
     */

    private static final int ASHIFT = 7;

    private static final int MMASK = 0xff;

    private static final int SEQ = MMASK + 1;

    private static final int NCPU = Runtime.getRuntime().availableProcessors();

    static final int FULL = (NCPU >= (MMASK << 1)) ? MMASK : NCPU >>> 1;

    private static final int SPINS = 1 << 10;

    private static final Object NULL_ITEM = new Object();

    private static final Object TIMED_OUT = new Object();

    // 节点用于保持需要交换的数据，缓存行对齐
    @sun.misc.Contended static final class Node {
        int index;              // arana数组下标，多槽位时使用
        int bound;              // 上一次记录的bound
        int collides;           // CAS失败次数
        int hash;               // 用于自旋的伪随机数
        Object item;            // 当前线程需要交换的数据
        volatile Object match;  // 匹配线程交换的数据
        volatile Thread parked; // 记录等待获取数据的线程
    }

    // 用户记录线程状态的内部类
    static final class Participant extends ThreadLocal<Node> {
        public Node initialValue() { return new Node(); }
    }

    // 记录线程状态
    private final Participant participant;
    // 多槽位数据交换使用
    private volatile Node[] arena;
    //用于交换数据的槽位
    private volatile Node slot;

    private volatile int bound;

    /**
     * 1. 从场地中选出偏移地址为(i << ASHIFT) + ABASE的内存值，也即第i个真正可用的Node，判断其槽位是否为空，
     *    为空，进入【步骤2】；不为空，说明有线程在此等待，尝试抢占该槽位，抢占成功，交换数据，并唤醒等待线程，返回，结束；
     *    没有抢占成功，进入【步骤9】
     * 2. 检查索引（i vs m）是否越界，越界，进入【步骤9】；没有越界，进入下一步。
     * 3. 尝试占有该槽位，抢占失败，进入【步骤1】；抢占成功，进入下一步。
     * 4. 检查match，是否有线程来交换数据，如果有，交换数据，结束；如果没有，进入下一步。
     * 5. 检查spin是否大于0，如果不大于0，进入下一步；如果大于0，检查hash是否小于0，并且spin减半或为0，
     *    如果不是，进入【步骤4】；如果是，让出CPU时间，过一会儿，进入【步骤4】
     * 6. 检查是否中断，m达到最小值，是否超时，如果没有中断，没有超时，并且m达到最小值，阻塞，过一会儿进入【步骤4】；否则，下一步。
     * 7. 没有线程来交换数据，尝试丢弃原有的槽位重新开始，丢弃失败，进入【步骤4】；否则，下一步。
     * 8. bound减1（m>0），索引减半；检查是否中断或超时，如果没有，进入【步骤1】；否则，返回，结束。
     * 9. 检查bound是否发生变化，如果变化了，重置collides，索引重置为m或左移，转向【步骤1】；否则，进入下一步。
     * 10. 检查collides是否达到最大值，如果没有，进入【步骤13】，否则下一步。
     * 11. m是否达到FULL，是，进入【步骤13】；否则，下一步。
     * 12. CAS bound加1是否成功，如果成功，i置为m+1，槽位增长，进入【步骤1】；否则，下一步。
     *
     * 13. collides加1，索引左移，进入【步骤1】
     */
    private final Object arenaExchange(Object item, boolean timed, long ns) {
        // 槽位数组
        Node[] a = arena;
        Node p = participant.get();
        for (int i = p.index;;) {                      // access slot at i
            int b, m, c; long j;                       // j is raw array offset
            // 在槽位数组中根据"索引" i 取出数据 j相当于是 "第一个"槽位
            Node q = (Node)U.getObjectVolatile(a, j = (i << ASHIFT) + ABASE);
            // 该位置上有数据(即有线程在这里等待交换数据)
            if (q != null && U.compareAndSwapObject(a, j, q, null)) {
                // 进行数据交换，这里和单槽位的交换是一样的
                Object v = q.item;                     // release
                q.match = item;
                Thread w = q.parked;
                if (w != null)
                    U.unpark(w);
                return v;
            }
            // bound 是最大的有效的位置，和MMASK相与，得到真正的存储数据的索引最大值
            // i 在这个范围内，该槽位也为空
            else if (i <= (m = (b = bound) & MMASK) && q == null) {
                // 将需要交换的数据 设置给p
                p.item = item;                         // offer
                if (U.compareAndSwapObject(a, j, null, p)) {
                    // 设置该槽位数据(在该槽位等待其它线程来交换数据
                    long end = (timed && m == 0) ? System.nanoTime() + ns : 0L;
                    Thread t = Thread.currentThread(); // wait
                    for (int h = p.hash, spins = SPINS;;) {
                        Object v = p.match;
                        // 在自旋的过程中，有线程来和该线程交换数据
                        if (v != null) {
                            //交换数据后，清空部分设置，返回交换得到的数据，over
                            U.putOrderedObject(p, MATCH, null);
                            p.item = null;             // clear for next use
                            p.hash = h;
                            return v;
                        }
                        else if (spins > 0) {
                            h ^= h << 1; h ^= h >>> 3; h ^= h << 10; // xorshift
                            if (h == 0)                // initialize hash
                                h = SPINS | (int)t.getId();
                            else if (h < 0 &&          // approx 50% true
                                     (--spins & ((SPINS >>> 1) - 1)) == 0)
                                Thread.yield();        // two yields per wait
                        }
                        // 交换数据的线程到来，但是还没有设置好match，再稍等一会
                        else if (U.getObjectVolatile(a, j) != p)
                            spins = SPINS;       // releaser hasn't set match yet
                        //符合条件，特别注意m==0 这个说明已经到达area 中最小的存储数据槽位了
                        //没有其他线程在槽位等待了，所有当前线程需要阻塞在这里
                        else if (!t.isInterrupted() && m == 0 &&
                                 (!timed ||
                                  (ns = end - System.nanoTime()) > 0L)) {
                            U.putObject(t, BLOCKER, this); // emulate LockSupport
                            p.parked = t;              // minimize window
                            // 再次检查槽位，看看在阻塞前，有没有线程来交换数据
                            if (U.getObjectVolatile(a, j) == p)
                                U.park(false, ns);
                            p.parked = null;
                            U.putObject(t, BLOCKER, null);
                        }
                        // 当前这个槽位一直没有线程来交换数据，准备换个槽位试试
                        else if (U.getObjectVolatile(a, j) == p &&
                                 U.compareAndSwapObject(a, j, p, null)) {
                            if (m != 0)                // try to shrink
                                U.compareAndSwapInt(this, BOUND, b, b + SEQ - 1);
                            p.item = null;
                            p.hash = h;
                            // 减小索引值 往"第一个"槽位的方向挪动
                            i = p.index >>>= 1;        // descend
                            if (Thread.interrupted())
                                return null;
                            if (timed && m == 0 && ns <= 0L)
                                return TIMED_OUT;
                            break;                     // expired; restart
                        }
                    }
                }
                else
                    // 占据槽位失败，先清空item,防止成功交换数据后，p.item还引用着item
                    p.item = null;                     // clear offer
            }
            else {
                if (p.bound != b) {                    // stale; reset
                    p.bound = b;
                    p.collides = 0;
                    i = (i != m || m == 0) ? m : m - 1;
                }
                else if ((c = p.collides) < m || m == FULL ||
                         !U.compareAndSwapInt(this, BOUND, b, b + SEQ + 1)) {
                    p.collides = c + 1;
                    i = (i == 0) ? m : i - 1;          // cyclically traverse
                }
                else
                    i = m + 1;                         // grow
                p.index = i;
            }
        }
    }

    /**
     *  1.当一个线程来交换数据时，如果发现槽位（solt）有数据时，说明其它线程已经占据了槽位，等待交换数据，
     *    那么当前线程就和该槽位进行数据交换，设置相应字段。
     *  2.如果交换失败，则说明其它线程抢先了该线程一步和槽位交换了数据，那么这个时候就存在竞争了，
     *    这个时候就会生成多槽位（area）,后面就会进行多槽位交换了。
     *  3.如果来交换的线程发现槽位没有被占据，这个时候自己就把槽位占据了，如果占据失败，则有可能其他线程抢先了占据了槽位，重头开始循环。
     *  4.当来交换的线程占据了槽位后，就需要等待其它线程来进行交换数据了，
     *    首先自己需要进行一定时间的自旋，因为自旋期间有可能其它线程就来了，那么这个时候就可以进行数据交换工作，而不用阻塞等待了；
     *    如果不幸，进行了一定自旋后，没有其他线程到来，那么还是避免不了需要阻塞（如果设置了超时等待，发生了超时或中断异常，则退出，不阻塞等待）。
     *    当准备阻塞线程的时候，发现槽位值变了，那么说明其它线程来交换数据了，但是还没有完全准备好数据，这个时候就不阻塞了，再稍微等那么一会；
     *    如果始终没有等到其它线程来交换，那么就挂起当前线程。
     *  5.当其它线程到来并成功交换数据后，会唤醒被阻塞的线程，阻塞的线程被唤醒后，拿到数据（如果是超时，或中断，则数据为null）返回，结束
     *
     */
    private final Object slotExchange(Object item, boolean timed, long ns) {
        // 从ThreadLocal获取Node节点
        Node p = participant.get();
        Thread t = Thread.currentThread();
        //如果当前线程中断，返回NULL
        if (t.isInterrupted()) // preserve interrupt status so caller can recheck
            return null;

        for (Node q;;) {
            // 槽位solt不为null,则说明已经有线程在这里等待交换数据了
            if ((q = slot) != null) {
                // 重置槽位
                if (U.compareAndSwapObject(this, SLOT, q, null)) {
                    // 获取交换的数据
                    Object v = q.item;
                    q.match = item;
                    Thread w = q.parked;
                    // 唤醒等待的线程
                    if (w != null)
                        U.unpark(w);
                    // 返回的是v，也就是需要交换的数据
                    return v;
                }
                // 存在竞争，其它线程抢先了一步该线程，因此需要采用多槽位模式，这个后面再分析
                // 实例化arena，然后继续执行循环，知道slot为空或者上面不为空情况，交换成功
                if (NCPU > 1 && bound == 0 &&
                    U.compareAndSwapInt(this, BOUND, 0, SEQ))
                    arena = new Node[(FULL + 2) << ASHIFT];
            }
            // 多槽位不为空，需要执行多槽位交换
            else if (arena != null)
                //直接返回了，说明单槽交换失败了
                return null;
            else {
                // 还没有其他线程来占据槽位，但 slot 为空
                p.item = item;
                // 设置槽位为p(也就是槽位被当前线程占据)
                // 槽位占成功，那么退出循
                if (U.compareAndSwapObject(this, SLOT, null, p))
                    break;
                //如果设置槽位失败，则有可能其他线程抢先了，重置item,重新循环
                p.item = null;
            }
        }

        // 当前线程占据槽位，等待其它线程来交换数据
        int h = p.hash;
        long end = timed ? System.nanoTime() + ns : 0L;
        int spins = (NCPU > 1) ? SPINS : 1;
        Object v;
        while ((v = p.match) == null) {
            if (spins > 0) {
                // 自旋
                h ^= h << 1; h ^= h >>> 3; h ^= h << 10;
                if (h == 0)
                    h = SPINS | (int)t.getId();
                else if (h < 0 && (--spins & ((SPINS >>> 1) - 1)) == 0)
                    // 让出cpu
                    Thread.yield();
            }
            //其它线程来交换数据了，修改了solt,但是还没有设置match,再稍等一会
            else if (slot != p)
                spins = SPINS;
            // 需要阻塞等待其它线程来交换数据
            // 没发生中断，并且是单槽交换，没有设置超时或者超时时间未到 则继续执行
            else if (!t.isInterrupted() && arena == null && (!timed || (ns = end - System.nanoTime()) > 0L)) {
                // cas将当前对象设置为BLOCKER
                U.putObject(t, BLOCKER, this);
                // 需要挂起当前线程
                p.parked = t;
                if (slot == p)
                    // 阻塞当前线程
                    U.park(false, ns);
                // 被唤醒后
                p.parked = null;
                U.putObject(t, BLOCKER, null);
            }
            else if (U.compareAndSwapObject(this, SLOT, p, null)) {
                v = timed && ns <= 0L && !t.isInterrupted() ? TIMED_OUT : null;
                break;
            }
        }
        // 清空match
        U.putOrderedObject(p, MATCH, null);
        p.item = null;
        p.hash = h;
        return v;
    }

    public Exchanger() {
        participant = new Participant();
    }

    @SuppressWarnings("unchecked")
    public V exchange(V x) throws InterruptedException {
        Object v;
        Object item = (x == null) ? NULL_ITEM : x; // translate null args
        // arena为空，说明是单槽操作，如果不为空，那么是多槽操作
        // 多槽操作是通过arenaExchange实现的
        if ((arena != null ||
             (v = slotExchange(item, false, 0L)) == null) &&
            ((Thread.interrupted() || // disambiguates null return
              (v = arenaExchange(item, false, 0L)) == null)))
            throw new InterruptedException();
        return (v == NULL_ITEM) ? null : (V)v;
    }

    @SuppressWarnings("unchecked")
    public V exchange(V x, long timeout, TimeUnit unit)
        throws InterruptedException, TimeoutException {
        Object v;
        Object item = (x == null) ? NULL_ITEM : x;
        long ns = unit.toNanos(timeout);
        if ((arena != null ||
             (v = slotExchange(item, true, ns)) == null) &&
            ((Thread.interrupted() ||
              (v = arenaExchange(item, true, ns)) == null)))
            throw new InterruptedException();
        if (v == TIMED_OUT)
            throw new TimeoutException();
        return (v == NULL_ITEM) ? null : (V)v;
    }

    // Unsafe mechanics
    private static final sun.misc.Unsafe U;
    private static final long BOUND;
    private static final long SLOT;
    private static final long MATCH;
    private static final long BLOCKER;
    private static final int ABASE;
    static {
        int s;
        try {
            U = sun.misc.Unsafe.getUnsafe();
            Class<?> ek = Exchanger.class;
            Class<?> nk = Node.class;
            Class<?> ak = Node[].class;
            Class<?> tk = Thread.class;
            BOUND = U.objectFieldOffset
                (ek.getDeclaredField("bound"));
            SLOT = U.objectFieldOffset
                (ek.getDeclaredField("slot"));
            MATCH = U.objectFieldOffset
                (nk.getDeclaredField("match"));
            BLOCKER = U.objectFieldOffset
                (tk.getDeclaredField("parkBlocker"));
            s = U.arrayIndexScale(ak);
            // ABASE absorbs padding in front of element 0
            ABASE = U.arrayBaseOffset(ak) + (1 << ASHIFT);

        } catch (Exception e) {
            throw new Error(e);
        }
        if ((s & (s-1)) != 0 || s > (1 << ASHIFT))
            throw new Error("Unsupported array scale");
    }

}
