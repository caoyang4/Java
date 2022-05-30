package src.rhino.threadpool.component;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import src.rhino.threadpool.RhinoThreadPoolMetric;

/**
 * RejectedExecutionHandler包装类，添加了统计拒绝请求的逻辑
 * Created by zmz on 2020/11/9.
 */
public class RhinoRejectedExecutionHandler implements RejectedExecutionHandler {

    private RejectedExecutionHandler originalHandler;
    private String rhinoKey;

    public RhinoRejectedExecutionHandler(String rhinoKey, RejectedExecutionHandler handler){
        this.rhinoKey = rhinoKey;
        this.originalHandler = handler;
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        RhinoThreadPoolMetric.rejectLog(rhinoKey);
        this.originalHandler.rejectedExecution(r, executor);
    }

    public RejectedExecutionHandler getOriginalHandler(){
        return originalHandler;
    }
}