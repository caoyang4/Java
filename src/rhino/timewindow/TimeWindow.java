package src.rhino.timewindow;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import src.rhino.util.CommonUtils;

/**
 * 滑动时间窗口，基于数组实现
 * Created by zmz on 2020/10/19.
 */
public abstract class TimeWindow<T extends TimeWindowData> {

    protected String rhinoKey;
    protected long timeLengthInMillis; //时间跨度
    protected int bucketCount; //桶数量
    private long bucketLength; //每个桶的时间跨度 = timeLengthInMillis / bucketCount

    protected final TimeWindowBucket<T>[] buckets;
    private final ReentrantLock resetLock = new ReentrantLock();

    public TimeWindow(String rhinoKey, long timeLengthInMillis, int bucketCount) {
        CommonUtils.assertTrue(timeLengthInMillis > 0, "timeLengthInSecond is invalid");
        CommonUtils.assertTrue(bucketCount > 0, "bucketCount is invalid");
        CommonUtils.assertTrue(timeLengthInMillis % bucketCount == 0, "timeLengthInSecond % bucketCount != 0");

        this.rhinoKey = rhinoKey;
        this.timeLengthInMillis = timeLengthInMillis;
        this.bucketCount = bucketCount;
        this.bucketLength = timeLengthInMillis / bucketCount;
        this.buckets = initBuckets(bucketCount);
    }

    private TimeWindowBucket[] initBuckets(int bucketCount){
        TimeWindowBucket[] buckets = new TimeWindowBucket[bucketCount];
        for(int i=0; i<bucketCount; i++){
            //初始化窗口全部为过期窗口
            buckets[i] = new TimeWindowBucket<T>(-1, bucketLength, newData());
        }
        return buckets;
    }

    /**
     * 初始化统计指标
     * @return
     */
    protected abstract T newData();

    /**
     * 根据时间戳计算bucket索引
     * @param timeMillis
     * @return
     */
    protected int calcBucketIndex(long timeMillis) {
        return (int) ((timeMillis / bucketLength) % bucketCount);
    }

    /**
     * 根据时间戳计算bucket起始时间
     * @param timeMillis
     * @return
     */
    protected long calcBucketStartTime(long timeMillis) {
        return timeMillis - timeMillis % bucketLength;
    }

    /**
     * 判断时间窗口是否过期
     * @param timestamp
     * @param bucket
     * @return
     */
    protected boolean isWindowDeprecated(long timestamp, TimeWindowBucket bucket){
        long startTime = bucket.getStartTime();
        if(startTime <= 0){
            return true;
        }
        return timestamp - bucket.getStartTime() > timeLengthInMillis;
    }

    public TimeWindowBucket<T> currentBucket() {
        return currentWindow(CommonUtils.currentMillis());
    }

    public TimeWindowBucket<T> currentBucket(long timeMillis) {
        return currentWindow(timeMillis);
    }

    private TimeWindowBucket<T> currentWindow(long timeMillis) {
        if (timeMillis < 0) {
            return null;
        }

        int idx = calcBucketIndex(timeMillis);
        long windowStart = calcBucketStartTime(timeMillis);

        for (int i=0; i<5; i++) {
            TimeWindowBucket old = buckets[idx];
            if (windowStart == old.getStartTime()) {
                //没有过期的，直接返回就行
                return old;
            } else if (windowStart > old.getStartTime()) {
                //已过期的，需要重置bucket，因为bucket重置不能保证原子性，这里加个锁
                if (resetLock.tryLock()) {
                    try {
                        return old.reset(windowStart);
                    } finally {
                        resetLock.unlock();
                    }
                } else {
                    //并发没抢到就等等
                    Thread.yield();
                }
            } else if (windowStart < old.getStartTime()) {
                // 理论上不存在这种情况，除非系统时间乱了
                return new TimeWindowBucket(windowStart, bucketLength, newData());
            }
        }

        //在高并发情况下未能抢到锁重置窗口，则放弃本次采样数据
        return new TimeWindowBucket(windowStart, bucketLength, newData());
    }

    /**
     * 返回指定时间内的有效窗口
     * @param timeMillis
     * @param n
     * @return
     */
    public List<TimeWindowBucket<T>> buckets(long timeMillis, int n){
        if (timeMillis < 0) {
            return new ArrayList<>();
        }

        n = Math.min(bucketCount, n);
        List<TimeWindowBucket<T>> result = new ArrayList<>(n);
        int start_index = calcBucketIndex(timeMillis);
        for (int i = 0; i < n; i++) {
            int index = (start_index + bucketCount - i ) % bucketCount;
            TimeWindowBucket<T> bucket = buckets[index];
            if(!isWindowDeprecated(timeMillis, bucket)){
                result.add(bucket);
            }
        }
        return result;
    }

    /**
     * 返回指定时间内的有效数据
     * @param timeMillis
     * @return
     */
    public List<T> values(long timeMillis, int n) {
        if (timeMillis < 0) {
            return new ArrayList<T>();
        }

        n = Math.min(bucketCount, n);
        List<T> result = new ArrayList<T>(n);
        int start_index = calcBucketIndex(timeMillis);
        for (int i = 0; i < n; i++) {
            int index = (start_index + bucketCount - i ) % bucketCount;
            TimeWindowBucket<T> bucket = buckets[index];
            if(!isWindowDeprecated(timeMillis, bucket)){
                result.add(bucket.getData());
            }
        }
        return result;
    }
}
