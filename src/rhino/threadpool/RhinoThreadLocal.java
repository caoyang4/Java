package src.rhino.threadpool;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zhanjun on 2018/8/1.
 */
public class RhinoThreadLocal<T> extends ThreadLocal<T> {

    public RhinoThreadLocal() {
        RhinoRequestContext.initializeContext();
    }

    @Override
    public void set(T value) {
        RhinoRequestContext.getContextForCurrentThread().state.put(this, new RhinoThreadLocal.LazyInitializer<T>(this, value));
    }

    @Override
    public T get() {
        if (RhinoRequestContext.getContextForCurrentThread() == null) {
            throw new IllegalStateException(RhinoRequestContext.class.getSimpleName() + ".initializeContext() must be called at the beginning of each request before RequestVariable functionality can be used.");
        }

        ConcurrentHashMap<RhinoThreadLocal<?>, RhinoThreadLocal.LazyInitializer<?>> variableMap = RhinoRequestContext.getContextForCurrentThread().state;

        // short-circuit the synchronized path below if we already have the value in the ConcurrentHashMap
        RhinoThreadLocal.LazyInitializer<?> v = variableMap.get(this);
        if (v != null) {
            return (T) v.get();
        }

        /*
         * Optimistically create a LazyInitializer to put into the ConcurrentHashMap.
         *
         * The LazyInitializer will not invoke initialValue() unless the get() method is invoked
         * so we can optimistically instantiate LazyInitializer and then discard for garbage collection
         * if the putIfAbsent fails.
         *
         * Whichever instance of LazyInitializer succeeds will then have get() invoked which will call
         * the initialValue() method once-and-only-once.
         */
        RhinoThreadLocal.LazyInitializer<T> l = new RhinoThreadLocal.LazyInitializer<T>(this);
        RhinoThreadLocal.LazyInitializer<?> existing = variableMap.putIfAbsent(this, l);
        if (existing == null) {
            /*
             * We won the thread-race so can use 'l' that we just created.
             */
            return l.get();
        } else {
            /*
             * We lost the thread-race so let 'l' be garbage collected and instead return 'existing'
             */
            return (T) existing.get();
        }
    }


    /**
     * Removes the value of the HystrixRequestVariable from the current request.
     * <p>
     * <p>
     * If the value is subsequently fetched in the thread, the {@link #initialValue} method will be called again.
     */
    @Override
    public void remove() {
        if (RhinoRequestContext.getContextForCurrentThread() != null) {
            remove(RhinoRequestContext.getContextForCurrentThread(), this);
        }
    }

    /**
     * @param context
     * @param v
     * @param <T>
     */
    static <T> void remove(RhinoRequestContext context, RhinoThreadLocal<T> v) {
        // remove first so no other threads get it
        context.state.remove(v);
    }

    static final class LazyInitializer<T> {
        // @GuardedBy("synchronization on get() or construction")
        private T value;

        /*
         * Boolean to ensure only-once initialValue() execution instead of using
         * a null check in case initialValue() returns null
         */
        // @GuardedBy("synchronization on get() or construction")
        private boolean initialized = false;

        private final RhinoThreadLocal<T> rv;

        private LazyInitializer(RhinoThreadLocal<T> rv) {
            this.rv = rv;
        }

        private LazyInitializer(RhinoThreadLocal<T> rv, T value) {
            this.rv = rv;
            this.value = value;
            this.initialized = true;
        }

        public synchronized T get() {
            if (!initialized) {
                value = rv.initialValue();
                initialized = true;
            }
            return value;
        }
    }
}
