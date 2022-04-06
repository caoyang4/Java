package src.juc.executors.threadPool.SelfRealizedPool;

/**
 * 简单实现拒绝策略
 * @author caoyang
 */
@FunctionalInterface
public interface SelfRejectPolicy<T> {
    /**
     * 策略模式调度
     */
    void reject(SelfBlockingQueue<T> queue, T task);
}
