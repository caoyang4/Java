package java.lang;
// java 祖宗类
public class Object {

    private static native void registerNatives();
    static {
        registerNatives();
    }
    // Class 对象
    public final native Class<?> getClass();

    public native int hashCode();

    public boolean equals(Object obj) {
        return (this == obj);
    }
    // clone，需实现 Clonable 接口
    protected native Object clone() throws CloneNotSupportedException;

    public String toString() {
        return getClass().getName() + "@" + Integer.toHexString(hashCode());
    }
    // 唤醒线程
    public final native void notify();

    public final native void notifyAll();

    public final native void wait(long timeout) throws InterruptedException;

    public final void wait(long timeout, int nanos) throws InterruptedException {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout value is negative");
        }

        if (nanos < 0 || nanos > 999999) {
            throw new IllegalArgumentException("nanosecond timeout value out of range");
        }

        if (nanos > 0) {
            timeout++;
        }

        wait(timeout);
    }
    // 线程阻塞，释放锁
    public final void wait() throws InterruptedException {
        wait(0);
    }
    // gc会调用该方法
    protected void finalize() throws Throwable { }
}
