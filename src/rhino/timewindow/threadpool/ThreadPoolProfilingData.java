package src.rhino.timewindow.threadpool;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import src.rhino.threadpool.ThreadPool;
import src.rhino.timewindow.TimeWindowData;
import src.rhino.util.LongAdder;

/**
 * 线程池监控数据
 * Created by zmz on 2020/10/20.
 */
public class ThreadPoolProfilingData implements TimeWindowData {

    private String rhinoKey;

    private AtomicInteger taskCount; //总任务数
    private LongAdder waitTime; //总排队时间
    private LongAdder executeTime; //总执行时间
    private AtomicLong maxWaitTime; //最大等待时间
    private AtomicLong maxExecuteTime; //最大执行时间0
    private AtomicInteger rejectCount; //拒绝任务数
    private AtomicInteger maxPoolSize; //最大线程数
    private AtomicInteger maxActiveCount; //最大活跃线程数
    private AtomicInteger maxQueueSize; //最大等待任务数

    private static final int CONCURRENT_FAILED_THRESHOLD = 5;

    public ThreadPoolProfilingData(String rhionKey){
        this.rhinoKey = rhionKey;
        this.taskCount = new AtomicInteger();
        this.waitTime = new LongAdder();
        this.maxWaitTime = new AtomicLong();
        this.executeTime = new LongAdder();
        this.maxExecuteTime = new AtomicLong();
        this.rejectCount = new AtomicInteger();
        this.maxPoolSize = new AtomicInteger();
        this.maxActiveCount = new AtomicInteger();
        this.maxQueueSize = new AtomicInteger();
    }

    public void executionProfiling(long taskWaitTimeMillis, long taskExecuteTimeMillis){
        taskCount.incrementAndGet();
        waitTime.add(taskWaitTimeMillis);
        updateMaxWaitTime(taskWaitTimeMillis);
        executeTime.add(taskExecuteTimeMillis);
        updateMaxExecuteTime(taskExecuteTimeMillis);
    }

    private void updateMaxWaitTime(long time){
        //在并发冲突的情况下限制自旋次数，避免吃掉cpu
        for(int i=0; i<CONCURRENT_FAILED_THRESHOLD; i++){
            long curr = maxWaitTime.get();
            if(curr >= time || maxWaitTime.compareAndSet(curr, time)){
                break;
            }
        }
    }

    private void updateMaxExecuteTime(long time){
        //在并发冲突的情况下限制自旋次数，避免吃掉cpu
        for(int i=0; i<CONCURRENT_FAILED_THRESHOLD; i++){
            long curr = maxExecuteTime.get();
            if(curr >= time || maxExecuteTime.compareAndSet(curr, time)){
                break;
            }
        }
    }

    /**
     * 总等待时间
     * @return
     */
    public long getTotalWaitTime(){
        return waitTime.sum();
    }

    /**
     * 平均任务等待时间
     * @return
     */
    public long getAvgWaitTime(){
        return avgTime(waitTime.sum(), taskCount.get());
    }

    /**
     * 最大任务等待时间
     * @return
     */
    public long getMaxWaitTime(){
        return maxWaitTime.get();
    }

    /**
     * 总执行时间
     * @return
     */
    public long getTotalExecuteTime(){
        return executeTime.sum();
    }

    /**
     * 平均任务执行时间
     * @return
     */
    public long getAvgExecuteTime(){
        return avgTime(executeTime.sum(), taskCount.get());
    }

    /**
     * 最大任务执行时间
     * @return
     */
    public long getMaxExecuteTime(){
        return maxExecuteTime.get();
    }

    public void updateMaxPoolSize(int count){
        //在并发冲突的情况下限制自旋次数，避免吃掉cpu
        for(int i=0; i<CONCURRENT_FAILED_THRESHOLD; i++){
            int curr = maxPoolSize.get();
            if(curr >= count || maxPoolSize.compareAndSet(curr, count)){
                break;
            }
        }
    }

    /**
     * 最大线程数
     * @return
     */
    public int getMaxPoolSize(){
        return maxPoolSize.get();
    }

    public void updateMaxActiveCount(int count){
        //在并发冲突的情况下限制自旋次数，避免吃掉cpu
        for(int i=0; i<CONCURRENT_FAILED_THRESHOLD; i++){
            int curr = maxActiveCount.get();
            if(curr >= count || maxActiveCount.compareAndSet(curr, count)){
                break;
            }
        }
    }

    /**
     * 最大活跃线程数量
     * @return
     */
    public int getMaxActiveCount(){
        return maxActiveCount.get();
    }

    public void updateMaxQueueSize(int count){
        //在并发冲突的情况下限制自旋次数，避免吃掉cpu
        for(int i=0; i<CONCURRENT_FAILED_THRESHOLD; i++){
            int curr = maxQueueSize.get();
            if(curr >= count || maxQueueSize.compareAndSet(curr, count)){
                break;
            }
        }
    }

    /**
     * 最大队列长度
     * @return
     */
    public int getMaxQueueSize(){
        return maxQueueSize.get();
    }

    public void reject(){
        rejectCount.incrementAndGet();
    }

    /**
     * 拒绝任务数
     * @return
     */
    public int getRejectCount(){
        return rejectCount.get();
    }

    /**
     * 总任务数（包括拒绝的）
     * @return
     */
    public int getTaskCount(){
        return taskCount.get();
    }

    @Override
    public void reset() {
        taskCount.set(0);
        waitTime.reset();
        executeTime.reset();
        maxWaitTime.set(0);
        maxExecuteTime.set(0);
        rejectCount.set(0);
        maxQueueSize.set(0);
        maxPoolSize.set(0);
        maxActiveCount.set(0);
    }

    @Override
    public String toString() {
        return "{" +
                "\"taskCount\":" + taskCount +
                "; \"waitTime\":" + waitTime +
                "; \"executeTime\":" + executeTime +
                "; \"maxWaitTime\":" + maxWaitTime +
                "; \"maxExecuteTime\":" + maxExecuteTime +
                "; \"rejectCount\":" + rejectCount +
                "; \"maxPoolSize\":" + maxPoolSize +
                "; \"maxActiveCount\":" + maxPoolSize +
                "; \"maxQueueSize\":" + maxQueueSize +
                '}';
    }

    public void setTaskCount(int count){
        this.taskCount.set(count);
    }

    public void setWaitTime(long waitTimeMillis){
        this.waitTime.reset();
        this.waitTime.add(waitTimeMillis);
    }

    public void setMaxWaitTime(long waitTimeMillis){
        this.maxWaitTime.set(waitTimeMillis);
    }

    public void setExecuteTime(long executeTimeMillis){
        this.executeTime.reset();
        this.executeTime.add(executeTimeMillis);
    }

    public void setMaxExecuteTime(long executeTimeMillis){
        this.maxExecuteTime.set(executeTimeMillis);
    }

    public void setRejectCount(int count){
        this.rejectCount.set(count);
    }

    public void setMaxPoolSize(int poolSize){
        this.maxPoolSize.set(poolSize);
    }

    public void setMaxActiveCount(int activeCount){
        this.maxActiveCount.set(activeCount);
    }

    public void setMaxQueueSize(int queueSize){
        this.maxQueueSize.set(queueSize);
    }

    private long avgTime(long total, long count){
        return count == 0 ? 0 : total/count;
    }
}
