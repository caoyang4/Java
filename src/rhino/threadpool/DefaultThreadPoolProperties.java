package src.rhino.threadpool;

import src.cat.Cat;
import src.rhino.RhinoConfigProperties;
import src.rhino.RhinoType;
import src.rhino.annotation.JsonIgnore;
import src.rhino.annotation.ThreadPoolExecute;
import src.rhino.config.ConfigChangedListener;
import src.rhino.config.Configuration;
import src.rhino.threadpool.bean.ThreadPoolBean;
import src.rhino.threadpool.component.*;
import src.rhino.util.AppUtils;
import src.rhino.util.SerializerUtils;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.concurrent.*;

/**
 * @author zhanjun on 2017/4/21.
 */
public class DefaultThreadPoolProperties extends RhinoConfigProperties implements ThreadPoolProperties {

    private volatile ThreadPoolBean threadPoolBean;

    @JsonIgnore
    private int keepAliveTimeMinutes;

    @JsonIgnore
    private TimeUnit keepAliveTimeUnit;

    @JsonIgnore
    private BlockingQueue<Runnable> blockingQueue;

    @JsonIgnore
    private RejectedExecutionHandler rejectHandler;

    @JsonIgnore
    private ThreadFactory threadFactory;

    @JsonIgnore
    private boolean prestartAllCoreThreads;

    private boolean traceable;

    public DefaultThreadPoolProperties(String rhinoKey) {
        this(rhinoKey, Setter());
    }

    public DefaultThreadPoolProperties(String rhinoKey, Setter setter) {
        this(AppUtils.getAppName(), rhinoKey, setter, null);
    }

    public DefaultThreadPoolProperties(String appKey, String rhinoKey, Setter setter, Configuration configuration) {
        super(appKey, rhinoKey, RhinoType.ThreadPool, configuration);
        ThreadPoolBean threadPoolBean = getBeanValue(configKeySuffix, ThreadPoolBean.class, null, true);
        if (setter == null) {
            setter = Setter();
        }
        if (threadPoolBean == null) {
            threadPoolBean = new ThreadPoolBean(setter);
        }
        this.threadPoolBean = threadPoolBean;

        //不可动态修改配置
        this.keepAliveTimeMinutes = setter.keepAliveTimeMinutes;
        this.keepAliveTimeUnit = setter.keepAliveTimeUnit;
        this.prestartAllCoreThreads = setter.prestartAllCoreThreads;
        this.rejectHandler = setter.rejectHandler;
        this.blockingQueue = buildBlockingQueue(setter, threadPoolBean);
        this.threadFactory = setter.threadFactory;
        this.traceable = setter.isTraceable();
    }

    public static Setter Setter() {
        return new Setter();
    }

    @Override
    public int getCoreSize() {
        return threadPoolBean.getCoreSize();
    }

    @Override
    public int getMaxSize() {
        return threadPoolBean.getMaxSize();
    }

    @Override
    public int getMaxQueueSize() {
        return threadPoolBean.getMaxQueueSize();
    }

    @Override
    public int getKeepAliveTimeMinutes() {
        return keepAliveTimeMinutes;
    }

    @Override
    public TimeUnit getKeepAliveTimeUnit() {
        return keepAliveTimeUnit;
    }

    @Override
    public boolean getPrestartAllCoreThreads() {
        return prestartAllCoreThreads;
    }

    @Override
    public boolean isTestIsolate() {
        return threadPoolBean.isTestIsolate();
    }

    @Override
    public RejectedExecutionHandler getRejectHandler() {
        return rejectHandler;
    }

    @Override
    public ShutdownPolicy getShutdownPolicy() {
        return threadPoolBean.getShutdownPolicy();
    }

    @Override
    public BlockingQueue<Runnable> getBlockingQueue() {
        return blockingQueue;
    }

    @Override
    public ThreadFactory getThreadFactory() {
        return threadFactory;
    }

    @Override
    public boolean isTraceable() {
        return traceable;
    }

    @Override
    public void addConfigChangedListener(ConfigChangedListener listener) {
        addPropertiesChangedListener(configKeySuffix, listener);
    }

    public void updateThreadPool(ThreadPool threadPool, String propStr) {
        try {
            threadPoolBean = SerializerUtils.read(URLDecoder.decode(propStr, "UTF-8"), ThreadPoolBean.class);
            threadPool.setCorePoolSize(threadPoolBean.getCoreSize());
            threadPool.setMaxPoolSize(threadPoolBean.getMaxSize());
            threadPool.setWorkQueueCapacity(threadPoolBean.getMaxQueueSize());
        } catch (Exception e) {
            logger.error("update thread pool properties error", e);
            Cat.logError(e);
        }
    }

    /**
     * 初始化阻塞队列
     *
     * @return
     */
    private BlockingQueue<Runnable> buildBlockingQueue(Setter setter, ThreadPoolBean threadPoolBean) {
        //通过API指定的阻塞队列
        if (setter.blockingQueue != null && threadPoolBean.getQueueType() == QueueType.SELF_DEFINITION) {
            return setter.blockingQueue;
        }

        //通过注解指定的阻塞队列
        Class<? extends BlockingQueue> queueClass = setter.blockingQueueByAnnotaion;
        if (!isDefaultBlockingQueue(queueClass)) {
            try {
                BlockingQueue<Runnable> blockingQueue = queueClass.newInstance();
                if (blockingQueue instanceof ResizableBlockingQueue) {
                    ((ResizableBlockingQueue<Runnable>) blockingQueue).setCapacity(setter.maxQueueSize);
                }
                return blockingQueue;
            } catch (Exception e) {
                throw new IllegalArgumentException("Can't create instance for " + queueClass.getName() + " :" + e.getMessage());
            }
        }

        //兼容以前老的默认是同步队列的类型
        if (threadPoolBean.getMaxQueueSize() <= 0 && QueueType.isResizableQueue(threadPoolBean.getQueueType())) {
            threadPoolBean.setQueueType(QueueType.SYNCHRONOUS_QUEUE);
        }

        return threadPoolBean.getQueueType().buildQueue(threadPoolBean.getMaxQueueSize());
    }

    private boolean isDefaultBlockingQueue(Class<? extends BlockingQueue> queueClass) {
        return queueClass == null || queueClass == ResizableBlockingQueue.class;
    }

    @Override
    public String toJson() {
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("{\"");
            builder.append(configKeySuffix);
            builder.append("\":");
            builder.append(SerializerUtils.write(threadPoolBean));
            builder.append("}");
            return builder.toString();
        } catch (IOException e) {
            logger.warn("DefaultThreadPoolProperties toJson error" + e.getMessage());
        }
        return "";
    }

    public static class Setter {
        private int coreSize;
        private int maxSize;
        private int keepAliveTimeMinutes;
        private TimeUnit keepAliveTimeUnit;
        private int maxQueueSize;
        private QueueType queueType;
        private BlockingQueue<Runnable> blockingQueue;
        private RejectedExecutionHandler rejectHandler;

        private boolean prestartAllCoreThreads;
        private boolean testIsolate;
        private ShutdownPolicy shutdownPolicy;
        private ThreadFactory threadFactory;
        private boolean traceable;

        //通过注解指定的阻塞队列
        private Class<? extends BlockingQueue> blockingQueueByAnnotaion;

        public Setter() {
            this.coreSize = default_coreSize;
            this.maxSize = default_maxSize;
            this.keepAliveTimeMinutes = default_keepAliveTimeMinutes;
            this.keepAliveTimeUnit = default_keepAliveTimeUnit;
            this.maxQueueSize = default_maxQueueSize;
            this.queueType = default_queueType;
            this.rejectHandler = default_RejectHandler;
            this.blockingQueue = null;
            this.blockingQueueByAnnotaion = null;

            this.prestartAllCoreThreads = default_preStartAllCoreThreads;
            this.testIsolate = default_testIsolate;
            this.shutdownPolicy = default_shutdownPolicy;
            this.threadFactory = null;
            this.traceable = default_traceable;
        }

        public Setter(ThreadPoolExecute threadPool) {
            this.coreSize = threadPool.coreSize();
            this.maxSize = threadPool.maxSize();
            this.keepAliveTimeMinutes = threadPool.keepAliveTimeMinutes();
            this.keepAliveTimeUnit = threadPool.keepAliveTimeUnit();
            this.maxQueueSize = threadPool.maxQueueSize();
            this.rejectHandler = constructRejectHandler(threadPool.rejectHandler());
            this.blockingQueue = null;
            this.blockingQueueByAnnotaion = threadPool.blockingQueue();
            this.queueType = threadPool.queueType();

            this.prestartAllCoreThreads = threadPool.prestartAllCoreThreads();
            this.testIsolate = threadPool.testIsolate();
            this.shutdownPolicy = threadPool.shutdownPolicy();
            this.threadFactory = null;
            this.traceable = threadPool.traceable();
        }

        /**
         * 创建拒绝策略
         *
         * @param handlerClz
         * @return
         */
        private RejectedExecutionHandler constructRejectHandler(Class<? extends RejectedExecutionHandler> handlerClz) {
            try {
                return handlerClz.newInstance();
            } catch (Exception e) {
                throw new IllegalArgumentException("Can't create instance for " + handlerClz.getName() + " :" + e.getMessage());
            }
        }

        public int getCoreSize() {
            return coreSize;
        }

        public int getMaxSize() {
            return maxSize;
        }

        public int getMaxQueueSize() {
            return maxQueueSize;
        }

        public int getKeepAliveTimeMinutes() {
            return keepAliveTimeMinutes;
        }

        public boolean isPrestartAllCoreThreads() {
            return prestartAllCoreThreads;
        }

        public boolean isTestIsolate() {
            return testIsolate;
        }

        public RejectedExecutionHandler getRejectHandler() {
            return this.rejectHandler;
        }

        public ShutdownPolicy getShutdownPolicy() {
            return this.shutdownPolicy;
        }

        public BlockingQueue<Runnable> getBlockingQueue() {
            return blockingQueue;
        }

        public ThreadFactory getThreadFactory() {
            return threadFactory;
        }

        public QueueType getQueueType() {
            return queueType;
        }

        public void setQueueType(QueueType queueType) {
            this.queueType = queueType;
        }

        public boolean isTraceable() {
            return traceable;
        }

        public Setter withCoreSize(int coreSize) {
            this.coreSize = coreSize;
            return this;
        }

        public Setter withMaxSize(int maxSize) {
            this.maxSize = maxSize;
            return this;
        }

        public Setter withMaxQueueSize(int maxQueueSize) {
            this.maxQueueSize = maxQueueSize;
            return this;
        }

        public Setter withKeepAliveTimeMinutes(int keepAliveTimeMinutes) {
            this.keepAliveTimeMinutes = keepAliveTimeMinutes;
            return this;
        }

        public Setter withKeepAliveTimeUnit(TimeUnit timeUnit) {
            this.keepAliveTimeUnit = timeUnit;
            return this;
        }

        public Setter withPrestartAllCoreThreads(boolean prestartAllCoreThreads) {
            this.prestartAllCoreThreads = prestartAllCoreThreads;
            return this;
        }

        public Setter withTestIsolate(boolean testIsolate) {
            this.testIsolate = testIsolate;
            return this;
        }

        public Setter withRejectHandler(RejectedExecutionHandler rejectHandler) {
            this.rejectHandler = rejectHandler;
            return this;
        }

        public Setter withShutdownPolicy(ShutdownPolicy shutdownPolicy) {
            this.shutdownPolicy = shutdownPolicy;
            return this;
        }

        public Setter withBlockingQueue(BlockingQueue<Runnable> blockingQueue) {
            this.blockingQueue = blockingQueue;
            this.maxQueueSize = blockingQueue.remainingCapacity();
            this.judgeQueueType();
            return this;
        }


        private void judgeQueueType() {
            if (blockingQueue == null) {
                return;
            }
            if (blockingQueue instanceof SynchronousQueue) {
                this.queueType = QueueType.SYNCHRONOUS_QUEUE;
            } else if (blockingQueue instanceof ResizableLinkedBlockingQueue) {
                this.queueType = QueueType.LINKED_BLOCKING_QUEUE;
            } else if (blockingQueue instanceof ResizablePriorityBlockingQueue) {
                this.queueType = QueueType.PRIORITY_BLOCKING_QUEUE;
            } else {
                this.queueType = QueueType.SELF_DEFINITION;
            }
        }

        public Setter withThreadFactory(ThreadFactory threadFactory) {
            this.threadFactory = threadFactory;
            return this;
        }

        public Setter withTraceable(boolean traceable) {
            this.traceable = traceable;
            return this;
        }
    }

}
