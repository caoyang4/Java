package java.util.concurrent.atomic;
import java.util.function.IntUnaryOperator;
import java.util.function.IntBinaryOperator;
import sun.misc.Unsafe;


public class AtomicIntegerArray implements java.io.Serializable {
    private static final long serialVersionUID = 2862133569453604235L;

    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final int base = unsafe.arrayBaseOffset(int[].class);
    private static final int shift;
    // 数组
    private final int[] array;

    static {
        int scale = unsafe.arrayIndexScale(int[].class);
        if ((scale & (scale - 1)) != 0)
            throw new Error("data type scale not a power of two");
        shift = 31 - Integer.numberOfLeadingZeros(scale);
    }

    private long checkedByteOffset(int i) {
        if (i < 0 || i >= array.length)
            throw new IndexOutOfBoundsException("index " + i);

        return byteOffset(i);
    }
    // 索引为 i 的偏移量
    private static long byteOffset(int i) {
        return ((long) i << shift) + base;
    }
    // 初始化方式一：给定长度，生成新数组
    public AtomicIntegerArray(int length) {
        array = new int[length];
    }
    // 初始化方式二：给定数组，将数组设置为数组的深拷贝，防止不经过这个类修改而是直接修改外界数组，导致结果错误
    public AtomicIntegerArray(int[] array) {
        // Visibility guaranteed by final field guarantees
        this.array = array.clone();
    }

    public final int length() {
        return array.length;
    }

    public final int get(int i) {
        return getRaw(checkedByteOffset(i));
    }

    private int getRaw(long offset) {
        return unsafe.getIntVolatile(array, offset);
    }

    public final void set(int i, int newValue) {
        unsafe.putIntVolatile(array, checkedByteOffset(i), newValue);
    }

    public final void lazySet(int i, int newValue) {
        unsafe.putOrderedInt(array, checkedByteOffset(i), newValue);
    }

    public final int getAndSet(int i, int newValue) {
        return unsafe.getAndSetInt(array, checkedByteOffset(i), newValue);
    }

    /**
     * @param i        索引
     * @param expect   旧值
     * @param update   新值
     */
    public final boolean compareAndSet(int i, int expect, int update) {
        return compareAndSetRaw(checkedByteOffset(i), expect, update);
    }

    private boolean compareAndSetRaw(long offset, int expect, int update) {
        return unsafe.compareAndSwapInt(array, offset, expect, update);
    }

    public final boolean weakCompareAndSet(int i, int expect, int update) {
        return compareAndSet(i, expect, update);
    }

    public final int getAndIncrement(int i) {
        return getAndAdd(i, 1);
    }

    public final int getAndDecrement(int i) {
        return getAndAdd(i, -1);
    }

    public final int getAndAdd(int i, int delta) {
        return unsafe.getAndAddInt(array, checkedByteOffset(i), delta);
    }

    public final int incrementAndGet(int i) {
        return getAndAdd(i, 1) + 1;
    }

    public final int decrementAndGet(int i) {
        return getAndAdd(i, -1) - 1;
    }

    public final int addAndGet(int i, int delta) {
        return getAndAdd(i, delta) + delta;
    }

    public final int getAndUpdate(int i, IntUnaryOperator updateFunction) {
        long offset = checkedByteOffset(i);
        int prev, next;
        do {
            prev = getRaw(offset);
            next = updateFunction.applyAsInt(prev);
        } while (!compareAndSetRaw(offset, prev, next));
        return prev;
    }

    public final int updateAndGet(int i, IntUnaryOperator updateFunction) {
        long offset = checkedByteOffset(i);
        int prev, next;
        do {
            prev = getRaw(offset);
            next = updateFunction.applyAsInt(prev);
        } while (!compareAndSetRaw(offset, prev, next));
        return next;
    }

    public final int getAndAccumulate(int i, int x,
                                      IntBinaryOperator accumulatorFunction) {
        long offset = checkedByteOffset(i);
        int prev, next;
        do {
            prev = getRaw(offset);
            next = accumulatorFunction.applyAsInt(prev, x);
        } while (!compareAndSetRaw(offset, prev, next));
        return prev;
    }

    public final int accumulateAndGet(int i, int x, IntBinaryOperator accumulatorFunction) {
        long offset = checkedByteOffset(i);
        int prev, next;
        do {
            prev = getRaw(offset);
            next = accumulatorFunction.applyAsInt(prev, x);
        } while (!compareAndSetRaw(offset, prev, next));
        return next;
    }

    public String toString() {
        int iMax = array.length - 1;
        if (iMax == -1)
            return "[]";

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            b.append(getRaw(byteOffset(i)));
            if (i == iMax)
                return b.append(']').toString();
            b.append(',').append(' ');
        }
    }

}
