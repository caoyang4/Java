package src.rhino.threadpool.job;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import src.rhino.threadpool.RhinoThreadPoolMetric;
import src.rhino.timewindow.threadpool.ThreadPoolProfilingSummary;
import src.rhino.timewindow.threadpool.ThreadPoolTimeWindow;

/**
 * 一次性任务，服务启动后注册CAT埋点收集器
 * Created by zmz on 2020/11/16.
 */
public class RhinoThreadPoolCatLog implements RhinoScheduledTask {

    @Override
    public String name() {
        return "RhinoThreadPoolCatLog";
    }

    @Override
    public long initialDelay() {
        return 0;
    }

    @Override
    public long period() {
        return 1;
    }

    @Override
    public TimeUnit periodUnit() {
        return TimeUnit.MINUTES;
    }

    @Override
    public void run() {
        /*StatusExtensionRegister.getInstance().register(new StatusExtension() {
            @Override
            public String getDescription() {
                return "rhino thread pool description";
            }

            @Override
            public String getId() {
                return "Rhino-ThreadPool";
            }

            @Override
            public Map<String, String> getProperties() {
                Map<String, String> data = new HashMap<>();
                for (ThreadPoolTimeWindow metric : RhinoThreadPoolMetric.minuteMetric.values()) {
                    String rhinoKey = metric.getRhinoKey();
                    ThreadPoolProfilingSummary summaryData = metric.summary(60);

                    //提交任务数量
                    data.put("rhino.threadpool.taskCount-" + rhinoKey, String.valueOf(summaryData.getTaskCount()));
                    //任务平均排队时间
                    data.put("rhino.threadpool.avgTaskWaitTime-" + rhinoKey, String.valueOf(summaryData.getAvgWaitTime()));
                    //任务最大排队时间
                    data.put("rhino.threadpool.maxTaskWaitTime-" + rhinoKey, String.valueOf(summaryData.getMaxWaitTime()));
                    //任务平均执行时间
                    data.put("rhino.threadpool.avgExecuteTime-" + rhinoKey, String.valueOf(summaryData.getAvgExecuteTime()));
                    //任务最大执行时间
                    data.put("rhino.threadpool.maxExecuteTime-" + rhinoKey, String.valueOf(summaryData.getMaxExecuteTime()));
                    //最大线程数量
                    data.put("rhino.threadpool.maxPoolSize-" + rhinoKey, String.valueOf(summaryData.getMaxPoolSize()));
                    //最大活跃线程数量
                    data.put("rhino.threadpool.maxActiveCount-" + rhinoKey, String.valueOf(summaryData.getMaxActiveCount()));
                    //拒绝任务数量
                    data.put("rhino.threadpool.rejectCount-" + rhinoKey, String.valueOf(summaryData.getRejectCount()));
                }
                return data;
            }
        });*/
    }
}
