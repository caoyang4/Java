package src.rhino.timewindow.threadpool;

import java.util.List;

import src.rhino.timewindow.TimeWindow;
import src.rhino.timewindow.TimeWindowBucket;
import src.rhino.util.CommonUtils;

/**
 * Created by zmz on 2020/10/20.
 */
public class ThreadPoolTimeWindow extends TimeWindow<ThreadPoolProfilingData> {

    public ThreadPoolTimeWindow(String rhinoKey, long timeLengthInMillis, int bucketCount) {
        super(rhinoKey, timeLengthInMillis, bucketCount);
    }

    @Override
    protected ThreadPoolProfilingData newData() {
        return new ThreadPoolProfilingData(rhinoKey);
    }

    /**
     * 记录任务等待总时间、任务执行总时间
     *
     * @param waitDuration
     * @param executeDuration
     */
    public void executionProfiling(long waitDuration, long executeDuration) {
        ThreadPoolProfilingData data = currentBucket().getData();
        data.executionProfiling(waitDuration, executeDuration);
    }

    /**
     * 记录拒绝任务数量
     */
    public void rejectProfiling() {
        ThreadPoolProfilingData data = currentBucket().getData();
        data.reject();
    }

    /**
     * 记录线程数量，队列大小
     *
     * @param poolSize
     * @param queueSize
     */
    public void threadProfiling(int poolSize, int activeCount, int queueSize) {
        ThreadPoolProfilingData data = currentBucket().getData();
        data.updateMaxPoolSize(poolSize);
        data.updateMaxActiveCount(activeCount);
        data.updateMaxQueueSize(queueSize);
    }

    public String getRhinoKey() {
        return rhinoKey;
    }

    /**
     * 汇总最近n个窗口内的统计数据
     * @param n
     * @return
     */
    public ThreadPoolProfilingSummary summary(int n) {
        ThreadPoolProfilingSummary summary = new ThreadPoolProfilingSummary();
        List<ThreadPoolProfilingData> allData = values(CommonUtils.currentMillis(), n);
        for (ThreadPoolProfilingData item : allData) {
            summary.addData(item);
        }
        return summary;
    }

    /**
     * 使用滑动窗口扫描数据，用于告警统计
     * @param timeLenght
     * @param windowWidth
     * @return
     */
    public ThreadPoolProfilingSummary scan(int timeSeconds, int windowWidth){
        windowWidth = ensureWindowLengthRange(windowWidth);

        //初始窗口
        ThreadPoolProfilingSummary windowSummary = new ThreadPoolProfilingSummary();
        long timeMillis = CommonUtils.currentMillis();
        int WindowRight = calcBucketIndex(timeMillis);
        int WindowLeft = (WindowRight - windowWidth + 1 + bucketCount) % bucketCount;
        for(int i=0; i < windowWidth; i++){
            int index = (WindowRight + bucketCount - i ) % bucketCount;
            TimeWindowBucket<ThreadPoolProfilingData> bucket = buckets[index];
            if(!isWindowDeprecated(timeMillis, bucket)){
                windowSummary.addData(bucket.getData());
            }
        }

        //窗口滑动
        ThreadPoolProfilingSummary summary = new ThreadPoolProfilingSummary();
        for(int i=0; i< timeSeconds; i++){
            summary.updateIfBigger(windowSummary);

            TimeWindowBucket<ThreadPoolProfilingData> moveOutData = buckets[WindowRight];
            WindowRight = (WindowRight - 1 + bucketCount) % bucketCount;
            WindowLeft = (WindowLeft - 1 + bucketCount) % bucketCount;
            TimeWindowBucket<ThreadPoolProfilingData> moveInData = buckets[WindowLeft];

            if(!isWindowDeprecated(timeMillis, moveOutData)){
                windowSummary.removeData(moveOutData.getData());
            }
            if(!isWindowDeprecated(timeMillis, moveInData)){
                windowSummary.addData(moveInData.getData());
            }
        }

        return summary;
    }

    private int ensureWindowLengthRange(int windowLength){
        return Math.max(1, Math.min(windowLength, 60));
    }
}
