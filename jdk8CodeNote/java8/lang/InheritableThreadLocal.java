package java.lang;
import java.lang.ref.*;

/**
 * ThreadLocal本身是线程隔离的，InheritableThreadLocal提供了一种父子线程之间的数据共享机制。
 * 它的具体实现是在Thread类中除了threadLocals外还有一个inheritableThreadLocals对象。
 * @param <T>
 */
public class InheritableThreadLocal<T> extends ThreadLocal<T> {

    protected T childValue(T parentValue) {
        return parentValue;
    }

    ThreadLocalMap getMap(Thread t) {
       return t.inheritableThreadLocals;
    }

    void createMap(Thread t, T firstValue) {
        t.inheritableThreadLocals = new ThreadLocalMap(this, firstValue);
    }
}
