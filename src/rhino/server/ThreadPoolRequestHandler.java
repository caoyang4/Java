package src.rhino.server;

import src.rhino.threadpool.RhinoThreadPoolMetric;
import src.rhino.threadpool.ThreadPool;
import src.rhino.timewindow.threadpool.ThreadPoolTimeWindow;
import src.rhino.util.CommonUtils;
import com.mysql.cj.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池接口
 * path: /threadpool
 * Created by zmz on 2020/10/29.
 */
public class ThreadPoolRequestHandler implements RhinoHttpRequestHandler {

    @Override
    public Object handleCommand(String command, String rhinoKey, String... params) {
        switch (command) {
            case "profiling":
                return getThreadPoolProfilingData(rhinoKey);
            case "config":
                return getThreadPoolConfig(rhinoKey);
            default:
                throw new IllegalArgumentException("Unsurpport command: " + command);
        }
    }

    /**
     * 获取线程池前一分钟的统计数据
     *
     * @param rhinoKey
     * @return
     */
    private Object getThreadPoolProfilingData(String rhinoKey) {
        CommonUtils.assertTrue(StringUtils.isNullOrEmpty(rhinoKey), "Thread pool not exist: " + rhinoKey);
        ThreadPoolTimeWindow timeWindow = RhinoThreadPoolMetric.current(rhinoKey);
        return timeWindow == null ? new ArrayList<>() : timeWindow.buckets(CommonUtils.currentMillis(), 60);
    }

    /**
     * 获取线程池的当前配置
     *
     * @param rhinoKey
     * @return
     */
    private Object getThreadPoolConfig(String rhinoKey) {
        ThreadPool instance = ThreadPool.Factory.get(rhinoKey);
        if (instance == null) {
            return null;
        }

        instance.getThreadPoolProperties();

        ThreadPoolExecutor executor = instance.getExecutor();
        Map<String, Object> config = new HashMap<>();
        config.put("coreSize", executor.getCorePoolSize());
        config.put("maxSize", executor.getMaximumPoolSize());
        config.put("queueSize", executor.getQueue().size());
        config.put("queueRemainingCapacity", executor.getQueue().remainingCapacity());
        config.put("queueType", executor.getQueue().getClass().getSimpleName());
        return config;
    }

}
