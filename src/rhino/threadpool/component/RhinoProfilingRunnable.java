package src.rhino.threadpool.component;

/**
 * Created by zmz on 2020/10/22.
 */
public class RhinoProfilingRunnable extends SubmitProfiling implements Runnable, Comparable<RhinoProfilingRunnable> {
    /**
     * 原生的任务
     */
    private Runnable origin;

    /**
     * 封装cat、mtrace后的任务
     */
    private Runnable target;

    public RhinoProfilingRunnable(Runnable origin, Runnable target, String rhinoKey) {
        super(rhinoKey);
        this.origin = origin;
        this.target = target;
    }

    @Override
    public void run() {
        try {
            execute();
            this.target.run();
        } finally {
            complete();
        }
    }

    @Override
    public int compareTo(RhinoProfilingRunnable o) {
        if (this.origin instanceof Comparable) {
            return ((Comparable) this.origin).compareTo(o.origin);
        }
        return 0;
    }
}
