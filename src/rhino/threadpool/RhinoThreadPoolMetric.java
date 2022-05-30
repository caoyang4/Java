package src.rhino.threadpool;

import java.util.concurrent.ConcurrentHashMap;

import src.rhino.timewindow.threadpool.ThreadPoolTimeWindow;
import src.rhino.timewindow.TimeWindowFactory;

/**
 * 线程池统计数据
 * Created by zmz on 2020/10/22.
 */
public class RhinoThreadPoolMetric {

    public static final ConcurrentHashMap<String, ThreadPoolTimeWindow> minuteMetric = new ConcurrentHashMap<>();

    private static ThreadPoolTimeWindow get(String rhinoKey) {
        ThreadPoolTimeWindow timeWindow = minuteMetric.get(rhinoKey);
        if (timeWindow == null) {
            timeWindow = TimeWindowFactory.newMinuteThreadPoolTimeWindow(rhinoKey);
            ThreadPoolTimeWindow oldWindow = minuteMetric.putIfAbsent(rhinoKey, timeWindow);
            if (oldWindow != null) {
                timeWindow = oldWindow;
            }
        }
        return timeWindow;
    }

    /**
     * 记录任务执行情况
     *
     * @param waitDuration
     * @param executeDuration
     */
    public static void executionLog(String rhinoKey, long waitDuration, long executeDuration) {
        ThreadPoolTimeWindow timeWindow = get(rhinoKey);
        timeWindow.executionProfiling(waitDuration, executeDuration);
    }

    /**
     * 记录线程池状态
     *
     * @param rhinoKey
     * @param queueSize
     * @param poolSize
     */
    public static void poolStatusLog(String rhinoKey, int poolSize, int activeCount, int queueSize) {
        ThreadPoolTimeWindow timeWindow = get(rhinoKey);
        timeWindow.threadProfiling(poolSize, activeCount, queueSize);
    }

    /**
     * 记录拒绝任务
     *
     * @param rhinoKey
     */
    public static void rejectLog(String rhinoKey) {
        ThreadPoolTimeWindow timeWindow = get(rhinoKey);
        timeWindow.rejectProfiling();
    }

    /**
     * 获取当前的统计数据
     *
     * @param rhinoKey
     * @return
     */
    public static ThreadPoolTimeWindow current(String rhinoKey) {
        return minuteMetric.get(rhinoKey);
    }
}
