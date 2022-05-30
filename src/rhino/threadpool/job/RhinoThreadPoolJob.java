package src.rhino.threadpool.job;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;

/**
 * 线程池相关的一些定时任务
 * Created by zmz on 2020/11/12.
 */
public class RhinoThreadPoolJob {

    private static final ScheduledExecutorService jobSchedulor =  Executors.newScheduledThreadPool(1, new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "rhino-threadpool-job");
        }
    });;

    private static final ConcurrentHashMap<String, ScheduledFuture> currentTask = new ConcurrentHashMap<>();

    public static void init(){
        shceduleOnce(new RhinoThreadPoolCatLog());
        schedule(new RhinoThreadPoolAlarmScanner());
    }

    public static void schedule(RhinoScheduledTask task){
        String name = task.getClass().getSimpleName();
        ScheduledFuture taskRuntime = jobSchedulor.scheduleAtFixedRate(task, task.initialDelay(), task.period(), task.periodUnit());
        currentTask.put(name, taskRuntime);
    }

    private static void shceduleOnce(RhinoScheduledTask task){
        jobSchedulor.schedule(task, task.initialDelay(), task.periodUnit());
    }

    public static <T extends RhinoScheduledTask> void cancel(Class<T> task){
        ScheduledFuture future = currentTask.remove(task.getSimpleName());
        if(future != null){
            future.cancel(true);
        }
    }


}
