package java.util.concurrent.atomic;
import java.util.function.LongBinaryOperator;
import java.util.function.DoubleBinaryOperator;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("serial")
abstract class Striped64 extends Number {
    @sun.misc.Contended static final class Cell {
        volatile long value;
        Cell(long x) { value = x; }
        final boolean cas(long cmp, long val) {
            return UNSAFE.compareAndSwapLong(this, valueOffset, cmp, val);
        }

        // Unsafe mechanics
        private static final sun.misc.Unsafe UNSAFE;
        private static final long valueOffset;
        static {
            try {
                UNSAFE = sun.misc.Unsafe.getUnsafe();
                Class<?> ak = Cell.class;
                valueOffset = UNSAFE.objectFieldOffset
                    (ak.getDeclaredField("value"));
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }

    static final int NCPU = Runtime.getRuntime().availableProcessors();
    // 提升性能发挥作用的Cell数组，核心思想是通过多个线程在对应自己的Cell进行累加，从而减少竞争。
    // 数量为2的n次幂，和hashmap一样，为了减少冲突概率
    transient volatile Cell[] cells;
    // 多个线程没有发生竞争的时候，值累加在base上，这与AtomicLong的value作用是一样的
    transient volatile long base;
    // Cells的锁标记，当Cells数组初始化，创建元素或者扩容的时候为1，否则为0
    transient volatile int cellsBusy;

    Striped64() {
    }

    final boolean casBase(long cmp, long val) {
        return UNSAFE.compareAndSwapLong(this, BASE, cmp, val);
    }

    final boolean casCellsBusy() {
        return UNSAFE.compareAndSwapInt(this, CELLSBUSY, 0, 1);
    }

    static final int getProbe() {
        return UNSAFE.getInt(Thread.currentThread(), PROBE);
    }

    static final int advanceProbe(int probe) {
        probe ^= probe << 13;   // xorshift
        probe ^= probe >>> 17;
        probe ^= probe << 5;
        UNSAFE.putInt(Thread.currentThread(), PROBE, probe);
        return probe;
    }

    /**
     * 可以重新改变table大小，或者创建新的cells。
     * @param x  增加的long值
     * @param fn 函数式编程，代表一个一个待执行操作的函数
     * @param wasUncontended
     *
     * 大致原理：
     * (1) Cell[]不为空，hash到的位置元素为空，那么就创建元素，并赋值为x，成功的话可以退出循环；
     * (2) Cell[]不为空，hash到的位置元素不为空，且上一轮cas修改失败了，这轮重试如果成功，可以退出循环；
     * (3) Cell[]为空，那么尝试初始化数组，并把x赋值到0或1号位置上，成功的话可以退出循环;
     * (4) Cell[]为空，且有其他线程在初始化数组，那么尝试累加到base上，成功的话可以退出循环；
     *  其他条件都是需要通过advanceProbe()进行rehash到其他位置，进行下一轮重试
     */
    final void longAccumulate(long x, LongBinaryOperator fn, boolean wasUncontended) {
        int h;
        // 如果没有初始化
        if ((h = getProbe()) == 0) {
            // current()里面会初始化probe值
            ThreadLocalRandom.current(); // force initialization
            // h 对应新的 probe 值, 用来对应 cell
            h = getProbe();
            // 还未初始化，肯定没有产生竞争
            wasUncontended = true;
        }
        // 是否发生碰撞，即多个线程hash到同一个Cell元素位置
        boolean collide = false;                // True if last slot nonempty
        for (;;) {
            Cell[] as; Cell a; int n; long v;
            // cells数组已经初始化
            if ((as = cells) != null && (n = as.length) > 0) {
                // hash到的数组元素位置为空
                if ((a = as[(n - 1) & h]) == null) {
                    // 为 cellsBusy 加锁, 创建 cell, cell 的初始累加值为 x
                    // 成功则 break, 否则继续 continue 循环
                    if (cellsBusy == 0) {       // Try to attach new Cell
                        // 新建 cell
                        Cell r = new Cell(x);   // Optimistically create
                        // 尝试获取锁
                        if (cellsBusy == 0 && casCellsBusy()) {
                            boolean created = false;
                            try {               // Recheck under lock
                                Cell[] rs; int m, j;
                                // 再次检查该位置元素是否为空
                                if ((rs = cells) != null && (m = rs.length) > 0 && rs[j = (m - 1) & h] == null) {
                                    // 将新生成的元素Cell(x)放在该位置上
                                    rs[j] = r;
                                    created = true;
                                }
                            } finally {
                                // 释放锁
                                cellsBusy = 0;
                            }
                            // (1)创建成功，退出循环
                            if (created)
                                break;
                            // 创建不成功，下一轮循环重试
                            continue;           // Slot is now non-empty
                        }
                    }
                    // 该位置元素为空，则没有发生碰撞
                    collide = false;
                }
                // 执行到此处，即该位置元素不为空，且cas失败了
                // 重置wasUncontended，通过下面的advanceProbe()重新hash，找到新的位置进行下一轮重试
                // 之所以重置wasUncontended，是为了下一轮重试时走下面cas分支，尝试对该位置元素进行值的修改
                else if (!wasUncontended)       // CAS already known to fail
                    wasUncontended = true;      // Continue after rehash
                // 重试，尝试对该位置元素进行值的修改，
                else if (a.cas(v = a.value, ((fn == null) ? v + x : fn.applyAsLong(v, x))))
                    // 修改成功退出循环
                    break;
                // 如果数组元素到达CPU个数或者已经被扩容了，则重新hash下一轮重试
                else if (n >= NCPU || cells != as)
                    collide = false;            // At max size or stale
                // 以上条件都不满足，则发生了碰撞，且竞争失败了
                else if (!collide)
                    collide = true;
                // 碰撞竞争失败时，则去尝试获取锁去扩容Cell数组
                else if (cellsBusy == 0 && casCellsBusy()) {
                    try {
                        if (cells == as) {      // Expand table unless stale
                            // 扩容为原来的2倍
                            Cell[] rs = new Cell[n << 1];
                            // 拷贝旧数组元素到新数组中
                            for (int i = 0; i < n; ++i)
                                rs[i] = as[i];
                            cells = rs;
                        }
                    } finally {
                        // 释放锁
                        cellsBusy = 0;
                    }
                    // 扩容成功，则重置collide，表示我有新的位置去重试了，不跟你抢这个位置了
                    collide = false;
                    continue;                   // Retry with expanded table
                }
                // 产生新的hash值，尝试去找别的数组位置
                h = advanceProbe(h);
            }
            // Cell[]为空，则尝试获取锁去初始化数组
            else if (cellsBusy == 0 && cells == as && casCellsBusy()) {
                boolean init = false;
                try {                           // Initialize table
                    if (cells == as) {
                        // 初始化大小为2
                        Cell[] rs = new Cell[2];
                        // 将Cell(x)放在0或1号位置上
                        rs[h & 1] = new Cell(x);
                        cells = rs;
                        init = true;
                    }
                } finally {
                    // 释放锁
                    cellsBusy = 0;
                }
                // 初始化成功，退出循环
                if (init)
                    break;
            }
            else if (casBase(v = base, ((fn == null) ? v + x : fn.applyAsLong(v, x))))
                // 成功则退出循环
                break;                          // Fall back on using base
        }
    }

    final void doubleAccumulate(double x, DoubleBinaryOperator fn, boolean wasUncontended) {
        int h;
        if ((h = getProbe()) == 0) {
            ThreadLocalRandom.current(); // force initialization
            h = getProbe();
            wasUncontended = true;
        }
        boolean collide = false;                // True if last slot nonempty
        for (;;) {
            Cell[] as; Cell a; int n; long v;
            if ((as = cells) != null && (n = as.length) > 0) {
                if ((a = as[(n - 1) & h]) == null) {
                    if (cellsBusy == 0) {       // Try to attach new Cell
                        Cell r = new Cell(Double.doubleToRawLongBits(x));
                        if (cellsBusy == 0 && casCellsBusy()) {
                            boolean created = false;
                            try {               // Recheck under lock
                                Cell[] rs; int m, j;
                                if ((rs = cells) != null &&
                                    (m = rs.length) > 0 &&
                                    rs[j = (m - 1) & h] == null) {
                                    rs[j] = r;
                                    created = true;
                                }
                            } finally {
                                cellsBusy = 0;
                            }
                            if (created)
                                break;
                            continue;           // Slot is now non-empty
                        }
                    }
                    collide = false;
                }
                else if (!wasUncontended)       // CAS already known to fail
                    wasUncontended = true;      // Continue after rehash
                else if (a.cas(v = a.value,
                               ((fn == null) ?
                                Double.doubleToRawLongBits
                                (Double.longBitsToDouble(v) + x) :
                                Double.doubleToRawLongBits
                                (fn.applyAsDouble
                                 (Double.longBitsToDouble(v), x)))))
                    break;
                else if (n >= NCPU || cells != as)
                    collide = false;            // At max size or stale
                else if (!collide)
                    collide = true;
                else if (cellsBusy == 0 && casCellsBusy()) {
                    try {
                        if (cells == as) {      // Expand table unless stale
                            Cell[] rs = new Cell[n << 1];
                            for (int i = 0; i < n; ++i)
                                rs[i] = as[i];
                            cells = rs;
                        }
                    } finally {
                        cellsBusy = 0;
                    }
                    collide = false;
                    continue;                   // Retry with expanded table
                }
                h = advanceProbe(h);
            }
            else if (cellsBusy == 0 && cells == as && casCellsBusy()) {
                boolean init = false;
                try {                           // Initialize table
                    if (cells == as) {
                        Cell[] rs = new Cell[2];
                        rs[h & 1] = new Cell(Double.doubleToRawLongBits(x));
                        cells = rs;
                        init = true;
                    }
                } finally {
                    cellsBusy = 0;
                }
                if (init)
                    break;
            }
            else if (casBase(v = base,
                             ((fn == null) ?
                              Double.doubleToRawLongBits
                              (Double.longBitsToDouble(v) + x) :
                              Double.doubleToRawLongBits
                              (fn.applyAsDouble
                               (Double.longBitsToDouble(v), x)))))
                break;                          // Fall back on using base
        }
    }

    // Unsafe mechanics
    private static final sun.misc.Unsafe UNSAFE;
    private static final long BASE;
    private static final long CELLSBUSY;
    private static final long PROBE;
    static {
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class<?> sk = Striped64.class;
            BASE = UNSAFE.objectFieldOffset
                (sk.getDeclaredField("base"));
            CELLSBUSY = UNSAFE.objectFieldOffset
                (sk.getDeclaredField("cellsBusy"));
            Class<?> tk = Thread.class;
            PROBE = UNSAFE.objectFieldOffset
                (tk.getDeclaredField("threadLocalRandomProbe"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }

}
