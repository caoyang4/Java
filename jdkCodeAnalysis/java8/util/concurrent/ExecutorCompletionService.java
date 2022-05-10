package java.util.concurrent;

public class ExecutorCompletionService<V> implements CompletionService<V> {
    // 执行任务的线程池
    private final Executor executor;
    private final AbstractExecutorService aes;
    // 任务完成会记录在该队列中
    private final BlockingQueue<Future<V>> completionQueue;

    // 用于放入执行完成的任务
    private class QueueingFuture extends FutureTask<Void> {
        QueueingFuture(RunnableFuture<V> task) {
            super(task, null);
            this.task = task;
        }
        // 重写了FutureTask的done方法，任务完成后，将任务放入阻塞队列中
        // finishCompletion方法最后调用
        protected void done() { completionQueue.add(task); }
        private final Future<V> task;
    }
    // 将传入的Callable包装为RunnableFuture
    private RunnableFuture<V> newTaskFor(Callable<V> task) {
        if (aes == null)
            return new FutureTask<V>(task);
        else
            return aes.newTaskFor(task);
    }
    //将传入的Runnable包装为RunnableFuture
    private RunnableFuture<V> newTaskFor(Runnable task, V result) {
        if (aes == null)
            return new FutureTask<V>(task, result);
        else
            return aes.newTaskFor(task, result);
    }

    public ExecutorCompletionService(Executor executor) {
        if (executor == null)
            throw new NullPointerException();
        this.executor = executor;
        this.aes = (executor instanceof AbstractExecutorService) ? (AbstractExecutorService) executor : null;
        // 阻塞队列默认LinkedBlockingQueue
        this.completionQueue = new LinkedBlockingQueue<Future<V>>();
    }

    public ExecutorCompletionService(Executor executor, BlockingQueue<Future<V>> completionQueue) {
        if (executor == null || completionQueue == null)
            throw new NullPointerException();
        this.executor = executor;
        this.aes = (executor instanceof AbstractExecutorService) ?
            (AbstractExecutorService) executor : null;
        this.completionQueue = completionQueue;
    }

    public Future<V> submit(Callable<V> task) {
        if (task == null) throw new NullPointerException();
        RunnableFuture<V> f = newTaskFor(task);
        executor.execute(new QueueingFuture(f));
        return f;
    }
    // 提交任务，任务被包装为QueueingFuture对象，主要重写FutureTask的done方法，
    // 使得任务执行完毕后被执行任务的线程放入到阻塞队列中
    public Future<V> submit(Runnable task, V result) {
        if (task == null) throw new NullPointerException();
        RunnableFuture<V> f = newTaskFor(task, result);
        executor.execute(new QueueingFuture(f));
        return f;
    }
    // 从阻塞队列中获取任务，如果任务还没有执行完，就阻塞
    public Future<V> take() throws InterruptedException {
        return completionQueue.take();
    }

    public Future<V> poll() {
        return completionQueue.poll();
    }

    public Future<V> poll(long timeout, TimeUnit unit)
            throws InterruptedException {
        return completionQueue.poll(timeout, unit);
    }

}
