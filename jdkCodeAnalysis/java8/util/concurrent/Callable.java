package java.util.concurrent;

/**
 * 函数式接口，获取线程执行结果
 * 相较于Runnable接口，Callable还可以抛出异常，这意味着如果在任务执行过程中发生了异常，我们可以将它向上抛出给任务的调用者来妥善处理，
 * 我们甚至可以利用这个特性来中断一个任务的执行。
 * 而Runnable接口的run方法不能抛出异常，只能在方法内部catch住处理，丧失了一定的灵活性
 */

@FunctionalInterface
public interface Callable<V> {
    // Callable有返回值
    // Callable可以抛出异常
    V call() throws Exception;
}
