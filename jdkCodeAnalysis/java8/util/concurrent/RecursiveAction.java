package java.util.concurrent;

/**
 * ForkJoinTask 是一个抽象类，在分治模型中，它还有两个抽象子类 RecursiveAction 和 RecursiveTask
 * RecursiveAction无返回值
 */
public abstract class RecursiveAction extends ForkJoinTask<Void> {
    private static final long serialVersionUID = 5232453952276485070L;

    protected abstract void compute();

    public final Void getRawResult() { return null; }

    protected final void setRawResult(Void mustBeNull) { }

    protected final boolean exec() {
        compute();
        return true;
    }

}
