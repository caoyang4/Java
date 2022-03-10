package src.juc.executors.threadPool.selfThreadPool;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 周期性任务线程池
 * @author caoyang
 */
public class ScheduledThreadPool{
    private final ScheduledExecutorService scheduledExecutorService;
    private final Map<String, Future<?>> futureMap = new HashMap<>();

    /**
     *  也是通过 ThreadPoolExecutor 创建线程池
     *  super(corePoolSize, Integer.MAX_VALUE, 0, NANOSECONDS, new DelayedWorkQueue());
     *  最大线程个数 Integer.MAX_VALUE
     *  空闲时间为 0
     */
    public ScheduledThreadPool(int poolSize) {
        scheduledExecutorService = Executors.newScheduledThreadPool(poolSize);
    }

    /**
     * 循环执行
     * @param task
     * @param delay
     * @param period
     * @param threadTag
     */
    public void cycleExecute(Runnable task, int delay, int period, String threadTag){
        Future<?> future = scheduledExecutorService.scheduleAtFixedRate(task, delay, period, TimeUnit.MILLISECONDS);
        futureMap.put(threadTag, future);
    }

    /**
     * 默认的循环执行,1秒执行一次
     * @param task
     */
    public void defaultCycleExecute(Runnable task){
        Future<?> future = scheduledExecutorService.scheduleAtFixedRate(task, 0, 1000, TimeUnit.SECONDS);
        futureMap.put(task.getClass().getName(), future);
    }

    /**
     * 延迟执行
     * @param task
     * @param delay
     */
    public void delayExecute(Runnable task, int delay){
        scheduledExecutorService.schedule(task, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * 是否在线程池中执行
     * @param threadTag
     * @return
     */
    public boolean isRunningInPool(String threadTag){
        return futureMap.get(threadTag) != null;
    }

    /**
     * 取消任务
     * @param threadTag
     */
    public void cancelTask(String threadTag){
        Future<?> future = futureMap.get(threadTag);
        if (future != null) {
            future.cancel(true);
            futureMap.remove(threadTag);
        }
    }

    /**
     * 关闭线程池
     */
    public void shutDown(){
        if (!scheduledExecutorService.isShutdown()){
            scheduledExecutorService.shutdown();
        }
    }

}
