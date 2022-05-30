package src.rhino.threadpool;

import java.util.concurrent.Callable;

/**
 * Created by zhanjun on 2018/8/1.
 */
public class RhinoWrapCallable<T> implements Callable<T>, Comparable<RhinoWrapCallable> {

    private RhinoRequestContext parentThreadState = RhinoRequestContext.getContextForCurrentThread();

    private Callable<T> actual;

    public RhinoWrapCallable(Callable<T> actual) {
        this.actual = actual;
    }

    @Override
    public T call() throws Exception {
        RhinoRequestContext existContext = RhinoRequestContext.getContextForCurrentThread();
        try {
            // set the state of this thread to that of it's parent
            RhinoRequestContext.setContextOnCurrentThread(parentThreadState);
            // execute actual with the state of the parent
            return actual.call();
        } finally {
            // restore this thread back to it's original state
            RhinoRequestContext.setContextOnCurrentThread(existContext);
        }
    }

    @Override
    public int compareTo(RhinoWrapCallable o) {
        if (this.actual instanceof Comparable) {
            return ((Comparable) this.actual).compareTo(o.actual);
        }
        return 0;
    }
}
