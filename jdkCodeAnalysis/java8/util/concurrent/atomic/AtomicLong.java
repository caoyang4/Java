package java.util.concurrent.atomic;
import java.util.function.LongUnaryOperator;
import java.util.function.LongBinaryOperator;
import sun.misc.Unsafe;

public class AtomicLong extends Number implements java.io.Serializable {
    private static final long serialVersionUID = 1927816293512124184L;

    // setup to use Unsafe.compareAndSwapLong for updates
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final long valueOffset;
    /**
     * 记录底层JVM是否支持longs的无锁compareAndSwap。
     * 虽然Unsafe.compareAndSwapLong方法在任何一种情况下都可以工作，
     * 但是应该在Java级别处理一些构造以避免锁定用户可见的锁。
     */
    static final boolean VM_SUPPORTS_LONG_CAS = VMSupportsCS8();
    // 返回底层JVM是否支持longs的无锁CompareAndSet。 仅调用一次并缓存在VM_SUPPORTS_LONG_CAS中
    private static native boolean VMSupportsCS8();

    static {
        try {
            valueOffset = unsafe.objectFieldOffset(AtomicLong.class.getDeclaredField("value"));
        } catch (Exception ex) { throw new Error(ex); }
    }
    // AtomicBoolean的关键值，value
    private volatile long value;

    public AtomicLong(long initialValue) {
        value = initialValue;
    }
    // 创建一个新的AtomicLong，初始值为 0
    public AtomicLong() {}

    public final long get() {
        return value;
    }

    /**
     *  set（）和lazySet（）有什么区别？
     *  1.首先set()是对volatile变量的一个写操作, 我们知道volatile的write为了保证对其他线程的可见性会追加以下两个Fence(内存屏障)如下：
     *     1.StoreStore 在intel cpu 不存在[写写]重排序
     *     2.StoreLoad 这个是所有内存屏障里最耗性能的内存屏障
     *  Doug Lea大大又说了, lazySet()省去了StoreLoad屏障, 只留下StoreStore
     *  总结：set()和volatile具有一样的效果(能够保证内存可见性，能够避免指令重排序)，
     *   但是使用lazySet不能保证其他线程能立刻看到修改后的值(有可能发生指令重排序)。
     *   简单点理解：lazySet比set()具有性能优势，但是使用场景很有限。
     */
    public final void set(long newValue) {
        value = newValue;
    }
    public final void lazySet(long newValue) {
        unsafe.putOrderedLong(this, valueOffset, newValue);
    }

    public final long getAndSet(long newValue) {
        return unsafe.getAndSetLong(this, valueOffset, newValue);
    }

    public final boolean compareAndSet(long expect, long update) {
        return unsafe.compareAndSwapLong(this, valueOffset, expect, update);
    }

    public final boolean weakCompareAndSet(long expect, long update) {
        return unsafe.compareAndSwapLong(this, valueOffset, expect, update);
    }

    public final long getAndIncrement() {
        return unsafe.getAndAddLong(this, valueOffset, 1L);
    }

    public final long getAndDecrement() {
        return unsafe.getAndAddLong(this, valueOffset, -1L);
    }

    public final long getAndAdd(long delta) {
        return unsafe.getAndAddLong(this, valueOffset, delta);
    }

    public final long incrementAndGet() {
        return unsafe.getAndAddLong(this, valueOffset, 1L) + 1L;
    }

    public final long decrementAndGet() {
        return unsafe.getAndAddLong(this, valueOffset, -1L) - 1L;
    }

    public final long addAndGet(long delta) {
        return unsafe.getAndAddLong(this, valueOffset, delta) + delta;
    }

    public final long getAndUpdate(LongUnaryOperator updateFunction) {
        long prev, next;
        do {
            prev = get();
            next = updateFunction.applyAsLong(prev);
        } while (!compareAndSet(prev, next));
        return prev;
    }

    public final long updateAndGet(LongUnaryOperator updateFunction) {
        long prev, next;
        do {
            prev = get();
            next = updateFunction.applyAsLong(prev);
        } while (!compareAndSet(prev, next));
        return next;
    }

    public final long getAndAccumulate(long x, LongBinaryOperator accumulatorFunction) {
        long prev, next;
        do {
            prev = get();
            next = accumulatorFunction.applyAsLong(prev, x);
        } while (!compareAndSet(prev, next));
        return prev;
    }

    public final long accumulateAndGet(long x, LongBinaryOperator accumulatorFunction) {
        long prev, next;
        do {
            prev = get();
            next = accumulatorFunction.applyAsLong(prev, x);
        } while (!compareAndSet(prev, next));
        return next;
    }

    public String toString() {
        return Long.toString(get());
    }

    public int intValue() {
        return (int)get();
    }

    public long longValue() {
        return get();
    }

    public float floatValue() {
        return (float)get();
    }

    public double doubleValue() {
        return (double)get();
    }

}
