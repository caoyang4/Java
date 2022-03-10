package src.juc.executors.threadPool.selfThreadPool;

import java.util.concurrent.Future;

/**
 * 线程池接口
 * @author caoyang
 */
public interface ThreadPoolInterface {
    /**
     * 提交任务, 获取任务执行结果
     * @param task
     * @return
     */
    Future<?> submit(Runnable task);

    /**
     * 提交反射任务，不带参数
     * @param o
     * @param methodName
     * @return
     */
    Future<?> submit(Object o, String methodName);

    /**
     * 执行任务
     * @param task
     */
    void execute(Runnable task);

    /**
     * 重载
     * 反射执行任务,不带参数
     * @param o
     * @param methodName
     */
    void execute(Object o, String methodName);

    /**
     * 取消任务
     * @param future
     * @return
     */
    boolean cancelTask(Future<?> future);

    /**
     * 关闭线程池
     */
    void shutdown();
}
