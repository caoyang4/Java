package java.util.concurrent.atomic;

public class AtomicStampedReference<V> {

    /**
     * Pair保存对象和对象版本
     */
    private static class Pair<T> {
        final T reference;
        final int stamp;
        private Pair(T reference, int stamp) {
            this.reference = reference;
            this.stamp = stamp;
        }
        static <T> Pair<T> of(T reference, int stamp) {
            return new Pair<T>(reference, stamp);
        }
    }

    private volatile Pair<V> pair;

    public AtomicStampedReference(V initialRef, int initialStamp) {
        pair = Pair.of(initialRef, initialStamp);
    }

    public V getReference() {
        return pair.reference;
    }

    public int getStamp() {
        return pair.stamp;
    }

    public V get(int[] stampHolder) {
        Pair<V> pair = this.pair;
        stampHolder[0] = pair.stamp;
        return pair.reference;
    }

    public boolean weakCompareAndSet(V   expectedReference, V   newReference, int expectedStamp, int newStamp) {
        return compareAndSet(expectedReference, newReference, expectedStamp, newStamp);
    }

    public boolean compareAndSet(V   expectedReference, V   newReference, int expectedStamp, int newStamp) {
        // pair 就是当前的值
        Pair<V> current = pair;
              // 旧数据与当前数据是否相等。如果是基础数据类型，则比较值；如果是引用数据类型，则判断的是内存地址。
        return expectedReference == current.reference &&
              // 旧版本号是否与当前的版本号相等。
              expectedStamp == current.stamp &&
                // 上面这两个条件，有一个不成立，就说明数据已经被别的线程已经修改过了，

                // 下面这两个条件就是为了防止做无用的 CAS 操作；也就是说，新数据、新版本号和当前的数据、版本号都是一样的，也没有别要再做 CAS 操作了。
              ((newReference == current.reference &&
              newStamp == current.stamp) ||
               // casPair才是主要做 CAS 操作的方法
              casPair(current, Pair.of(newReference, newStamp)));
    }

    public void set(V newReference, int newStamp) {
        Pair<V> current = pair;
        if (newReference != current.reference || newStamp != current.stamp)
            this.pair = Pair.of(newReference, newStamp);
    }

    public boolean attemptStamp(V expectedReference, int newStamp) {
        Pair<V> current = pair;
        return
            expectedReference == current.reference &&
            (newStamp == current.stamp ||
             casPair(current, Pair.of(expectedReference, newStamp)));
    }

    // Unsafe mechanics

    private static final sun.misc.Unsafe UNSAFE = sun.misc.Unsafe.getUnsafe();
    private static final long pairOffset =
        objectFieldOffset(UNSAFE, "pair", AtomicStampedReference.class);

    private boolean casPair(Pair<V> cmp, Pair<V> val) {
        return UNSAFE.compareAndSwapObject(this, pairOffset, cmp, val);
    }

    static long objectFieldOffset(sun.misc.Unsafe UNSAFE,
                                  String field, Class<?> klazz) {
        try {
            return UNSAFE.objectFieldOffset(klazz.getDeclaredField(field));
        } catch (NoSuchFieldException e) {
            // Convert Exception to corresponding Error
            NoSuchFieldError error = new NoSuchFieldError(field);
            error.initCause(e);
            throw error;
        }
    }
}
