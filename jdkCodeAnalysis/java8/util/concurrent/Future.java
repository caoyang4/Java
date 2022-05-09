package java.util.concurrent;

/**
 * Future接口被设计用来代表一个异步操作的执行结果。
 * 可以用它来获取一个操作的执行结果、取消一个操作、判断一个操作是否已经完成或者是否被取消
 */
public interface Future<V> {

    /**
     * 取消任务
     * cancel操作返回true并不代表任务真的就是被取消了，这取决于发动cancel状态时任务所处的状态：
     *
     * 如果发起cancel时任务还没有开始运行，则随后任务就不会被执行；
     * 如果发起cancel时任务已经在运行了，则这时就需要看mayInterruptIfRunning参数了：
     *   如果mayInterruptIfRunning 为true, 则当前在执行的任务会被中断
     *   如果mayInterruptIfRunning 为false, 则可以允许正在执行的任务继续运行，直到它执行完
     */
    boolean cancel(boolean mayInterruptIfRunning);
    // 任务是否取消
    boolean isCancelled();
    // 任务是否执行完成
    // 1. 任务正常执行完毕
    // 2. 任务抛出了异常
    // 3. 任务已经被取消
    boolean isDone();
    // 获取任务执行结果
    V get() throws InterruptedException, ExecutionException;

    V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;
}
