package src.rhino.timewindow;

import src.rhino.timewindow.system.SystemTimeWindow;
import src.rhino.timewindow.threadpool.ThreadPoolTimeWindow;

/**
 * 时间窗口工厂类
 * Created by zmz on 2020/10/27.
 */
public class TimeWindowFactory {

    /**
     * 线程池时间窗口，统计时间一分钟
     *
     * @return
     */
    public static ThreadPoolTimeWindow newMinuteThreadPoolTimeWindow(String rhinoKey) {
        return new ThreadPoolTimeWindow(rhinoKey, 90 * 1000L, 90);
    }

    /**
     * CPU使用率时间窗口，统计时间8s，50ms一个桶
     *
     * @return
     */
    public static SystemTimeWindow newSystemTimeWindow(String rhinoKey) {
        return new SystemTimeWindow(rhinoKey, 8 * 1000L, 160);
    }
}
