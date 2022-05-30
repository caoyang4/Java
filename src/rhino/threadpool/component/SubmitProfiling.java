package src.rhino.threadpool.component;

import src.rhino.threadpool.RhinoThreadPoolMetric;
import src.rhino.util.CommonUtils;

/**
 * 线程池任务提交数据统计
 * Created by zmz on 2020/10/21.
 */
public class SubmitProfiling {
    protected String rhinoKey;
    private long submitTime;
    private long executeTime;
    private long completeTime;

    protected SubmitProfiling(String rhinoKey) {
        this.rhinoKey = rhinoKey;
        this.submitTime = CommonUtils.currentMillis();
    }

    protected void execute() {
        this.executeTime = CommonUtils.currentMillis();
    }

    protected void complete() {
        this.completeTime = CommonUtils.currentMillis();
        logMetirc();
    }

    private void logMetirc() {
        long waitDuration = Math.max(executeTime - submitTime, 0);
        long executeDuration = Math.max(completeTime - executeTime, 0);
        RhinoThreadPoolMetric.executionLog(rhinoKey, waitDuration, executeDuration);
    }
}
