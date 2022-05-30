package src.rhino.timewindow.threadpool;


public class ThreadPoolProfilingSummary {
    private int taskCount;
    private long waitTime;
    private long executeTime;
    private long maxWaitTime;
    private long maxExecuteTime;
    private int rejectCount;
    private int maxPoolSize;
    private int maxActiveCount;
    private int maxQueueSize;

    public void addData(ThreadPoolProfilingData data){
        this.taskCount += data.getTaskCount();
        this.waitTime += data.getTotalWaitTime();
        this.maxWaitTime = Math.max(this.maxWaitTime, data.getMaxWaitTime());
        this.executeTime += data.getTotalExecuteTime();
        this.maxExecuteTime = Math.max(this.maxExecuteTime, data.getMaxExecuteTime());
        this.rejectCount += data.getRejectCount();
        this.maxPoolSize = Math.max(this.maxPoolSize, data.getMaxPoolSize());
        this.maxActiveCount = Math.max(this.maxActiveCount, data.getMaxActiveCount());
        this.maxQueueSize = Math.max(this.maxQueueSize, data.getMaxQueueSize());
    }

    public void removeData(ThreadPoolProfilingData data){
        this.taskCount -= data.getTaskCount();
        this.waitTime -= data.getTotalWaitTime();
        this.executeTime -= data.getTotalExecuteTime();
        this.rejectCount -= data.getRejectCount();
    }

    public void updateIfBigger(ThreadPoolProfilingSummary other){
        this.taskCount = Math.max(this.taskCount, other.getTaskCount());
        this.waitTime = Math.max(this.waitTime, other.getWaitTime());
        this.maxWaitTime = Math.max(this.maxWaitTime, other.getMaxWaitTime());
        this.executeTime = Math.max(this.executeTime, other.getExecuteTime());
        this.maxExecuteTime = Math.max(this.maxExecuteTime, other.getMaxExecuteTime());
        this.rejectCount = Math.max(this.rejectCount, other.getRejectCount());
        this.maxPoolSize = Math.max(this.maxPoolSize, other.getMaxPoolSize());
        this.maxActiveCount = Math.max(this.maxActiveCount, other.getMaxActiveCount());
        this.maxQueueSize = Math.max(this.maxQueueSize, other.getMaxQueueSize());
    }

    public long getAvgWaitTime(){
        return taskCount == 0 ? 0 : waitTime/taskCount;
    }

    public long getAvgExecuteTime(){
        return taskCount == 0 ? 0 : executeTime/taskCount;
    }

    public int getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(int taskCount) {
        this.taskCount = taskCount;
    }

    public long getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(long waitTime) {
        this.waitTime = waitTime;
    }

    public long getExecuteTime() {
        return executeTime;
    }

    public void setExecuteTime(long executeTime) {
        this.executeTime = executeTime;
    }

    public long getMaxWaitTime() {
        return maxWaitTime;
    }

    public void setMaxWaitTime(long maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
    }

    public long getMaxExecuteTime() {
        return maxExecuteTime;
    }

    public void setMaxExecuteTime(long maxExecuteTime) {
        this.maxExecuteTime = maxExecuteTime;
    }

    public int getRejectCount() {
        return rejectCount;
    }

    public void setRejectCount(int rejectCount) {
        this.rejectCount = rejectCount;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getMaxActiveCount() {
        return maxActiveCount;
    }

    public void setMaxActiveCount(int maxActiveCount) {
        this.maxActiveCount = maxActiveCount;
    }

    public int getMaxQueueSize() {
        return maxQueueSize;
    }

    public void setMaxQueueSize(int maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
    }

    @Override
    public String toString() {
        return "ThreadPoolProfilingSummary{" +
                "taskCount=" + taskCount +
                ", waitTime=" + waitTime +
                ", executeTime=" + executeTime +
                ", maxWaitTime=" + maxWaitTime +
                ", maxExecuteTime=" + maxExecuteTime +
                ", rejectCount=" + rejectCount +
                ", maxPoolSize=" + maxPoolSize +
                ", maxActiveCount=" + maxActiveCount +
                ", maxQueueSize=" + maxQueueSize +
                '}';
    }
}
