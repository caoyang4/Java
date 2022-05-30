package src.rhino.threadpool.bean;

import src.rhino.threadpool.DefaultThreadPoolProperties;
import src.rhino.threadpool.component.QueueType;
import src.rhino.threadpool.component.ShutdownPolicy;

/**
 * 线程池远程配置
 * Created by wanghao on 18/2/1.
 */
public class ThreadPoolBean {
    private int coreSize;
    private int maxSize;
    private int maxQueueSize;

    /**
     * 队列类型
     */
    private QueueType queueType = QueueType.LINKED_BLOCKING_QUEUE;

    /**
     * JVM进程关闭是，线程池的关闭处理策略
     * 兼容旧版本，默认策略是立即关闭
     *
     * @since 1.2.8
     */
    private ShutdownPolicy shutdownPolicy = ShutdownPolicy.DO_NOTHING;

    /**
     * 是否隔离压测线程池和正常线程池
     *
     * @since 1.2.6.3
     */
    private boolean testIsolate;

    public ThreadPoolBean() {
    }

    public ThreadPoolBean(DefaultThreadPoolProperties.Setter setter) {
        this.coreSize = setter.getCoreSize();
        this.maxSize = setter.getMaxSize();
        this.maxQueueSize = setter.getMaxQueueSize();
        this.queueType = setter.getQueueType();

        this.shutdownPolicy = setter.getShutdownPolicy();
        this.testIsolate = setter.isTestIsolate();
    }

    public int getCoreSize() {
        return coreSize;
    }

    public void setCoreSize(int coreSize) {
        this.coreSize = coreSize;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public int getMaxQueueSize() {
        return maxQueueSize;
    }

    public void setMaxQueueSize(int maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
    }

    public QueueType getQueueType() {
        return queueType;
    }

    public void setQueueType(QueueType queueType) {
        this.queueType = queueType;
    }

    public boolean isTestIsolate() {
        return testIsolate;
    }

    public void setTestIsolate(boolean testIsolate) {
        this.testIsolate = testIsolate;
    }

    public ShutdownPolicy getShutdownPolicy() {
        return shutdownPolicy;
    }

    public void setShutdownPolicy(ShutdownPolicy shutdownPolicy) {
        this.shutdownPolicy = shutdownPolicy;
    }

    @Override
    public String toString() {
        return "ThreadPoolBean{" +
                "coreSize=" + coreSize +
                ", maxSize=" + maxSize +
                ", maxQueueSize=" + maxQueueSize +
                ", queueType=" + queueType +
                ", shutdownPolicy=" + shutdownPolicy +
                ", testIsolate=" + testIsolate +
                '}';
    }
}
