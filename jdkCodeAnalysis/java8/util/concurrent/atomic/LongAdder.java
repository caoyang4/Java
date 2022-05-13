package java.util.concurrent.atomic;
import java.io.Serializable;

/**
 * 继承Striped64
 */
public class LongAdder extends Striped64 implements Serializable {
    private static final long serialVersionUID = 7249069246863182397L;

    public LongAdder() {
    }

    public void add(long x) {
        // as 为累加单元数组
        // b 为基础值
        // x 为累加值
        Cell[] as; long b, v; int m; Cell a;
        // 进入 if 的两个条件
        // 1. as 有值, 表示已经发生过竞争
        // 2. cas 给 base 累加时失败了, 表示 base 发生了竞争
        if ((as = cells) != null || !casBase(b = base, b + x)) {
            boolean uncontended = true;
            // as 还没有创建
            if (as == null || (m = as.length - 1) < 0 ||
                // 当前线程对应的 cell 还没有
                (a = as[getProbe() & m]) == null ||
                // cas 给当前线程的 cell 累加失败 uncontended=false ( a 为当前线程的 cell )
                !(uncontended = a.cas(v = a.value, v + x)))
                // 进入 cell 数组创建、cell 创建的流程
                longAccumulate(x, null, uncontended);
        }
    }

    public void increment() {
        add(1L);
    }

    public void decrement() {
        add(-1L);
    }
    // 将 base 和 cells 数组中的值累加就是最终的值
    public long sum() {
        Cell[] as = cells; Cell a;
        long sum = base;
        if (as != null) {
            for (int i = 0; i < as.length; ++i) {
                if ((a = as[i]) != null)
                    sum += a.value;
            }
        }
        return sum;
    }

    public void reset() {
        Cell[] as = cells; Cell a;
        base = 0L;
        if (as != null) {
            for (int i = 0; i < as.length; ++i) {
                if ((a = as[i]) != null)
                    a.value = 0L;
            }
        }
    }

    public long sumThenReset() {
        Cell[] as = cells; Cell a;
        long sum = base;
        base = 0L;
        if (as != null) {
            for (int i = 0; i < as.length; ++i) {
                if ((a = as[i]) != null) {
                    sum += a.value;
                    a.value = 0L;
                }
            }
        }
        return sum;
    }

    public String toString() {
        return Long.toString(sum());
    }

    public long longValue() {
        return sum();
    }

    public int intValue() {
        return (int)sum();
    }

    public float floatValue() {
        return (float)sum();
    }

    public double doubleValue() {
        return (double)sum();
    }

    private static class SerializationProxy implements Serializable {
        private static final long serialVersionUID = 7249069246863182397L;

        private final long value;

        SerializationProxy(LongAdder a) {
            value = a.sum();
        }

        private Object readResolve() {
            LongAdder a = new LongAdder();
            a.base = value;
            return a;
        }
    }

    private Object writeReplace() {
        return new SerializationProxy(this);
    }

    private void readObject(java.io.ObjectInputStream s)
        throws java.io.InvalidObjectException {
        throw new java.io.InvalidObjectException("Proxy required");
    }

}
