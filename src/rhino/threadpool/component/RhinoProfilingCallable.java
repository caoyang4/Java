package src.rhino.threadpool.component;

import java.util.concurrent.Callable;

/**
 * Created by zmz on 2020/10/22.
 */
public class RhinoProfilingCallable<T> extends SubmitProfiling implements Callable<T>, Comparable<RhinoProfilingCallable> {

    /**
     * 原生的任务
     */
    private Callable<T> origin;

    /**
     * cat、mtrace封装后的任务
     */
    private Callable<T> target;

    public RhinoProfilingCallable(Callable<T> origin, Callable<T> target, String rhinoKey) {
        super(rhinoKey);
        this.origin = origin;
        this.target = target;
    }

    @Override
    public T call() throws Exception {
        try {
            execute();
            return origin.call();
        } finally {
            complete();
        }
    }

    @Override
    public int compareTo(RhinoProfilingCallable o) {
        if (this.origin instanceof Comparable) {
            return ((Comparable) this.origin).compareTo(o.origin);
        }
        return 0;
    }
}
