package src.rhino.threadpool;

import src.rhino.log.Logger;
import src.rhino.log.LoggerFactory;
import src.rhino.threadpool.component.RhinoProfilingCallable;
import src.rhino.threadpool.component.RhinoProfilingRunnable;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * Rhino线程池任务工厂类
 * 封装线程任务，适配一些中间件的跨线程追踪特性
 *
 * @author zhanjun on 2017/09/13.
 */
public class TaskFactory {

    private static final Logger logger = LoggerFactory.getLogger(TaskFactory.class);
    private static Method callableGetMethod;
    private static Method runnableGetMethod;

    static {
        try {
            Class callableClass = Class.forName("com.meituan.mtrace.thread.TraceCallable");
            Class runnableClass = Class.forName("com.meituan.mtrace.thread.TraceRunnable");
            callableGetMethod = callableClass.getDeclaredMethod("get", Callable.class);
            runnableGetMethod = runnableClass.getDeclaredMethod("get", Runnable.class);
        } catch (Exception e) {
            logger.warn("TraceCallable.get(Callable<T> callable) or TraceRunnable.get(Runnable runnable) method init error", e);
        }
    }

    /**
     * 兼容旧版本
     * 必须先分装cat，然后封装mtrace，否则cat埋点无法获取压测标识
     *
     * @param task
     * @param <T>
     * @return
     */
    public static <T> Callable<T> wrap(Callable<T> task) {
        Callable finalTask = task;
        finalTask = CatWrapper(finalTask);              //cat透传
        finalTask = MtraceWrapper(finalTask);           //mtrace透传
        return finalTask;
    }

    public static <T> Callable<T> wrap(Callable<T> task, String rhinoKey) {
        Callable finalTask = wrap(task);
        finalTask = RhinoWrapper(task, finalTask, rhinoKey);  //rhino监控与告警
        return finalTask;
    }

    public static <T> Callable<T> wrap(Callable<T> task, String rhinoKey, boolean traceable) {
        Callable finalTask = task;
        if (traceable) {
            finalTask = wrap(task);
        }
        finalTask = RhinoWrapper(task, finalTask, rhinoKey);  //rhino监控与告警
        return finalTask;
    }

    private static <T> Callable<T> MtraceWrapper(Callable<T> task) {
        if (callableGetMethod != null) {
            try {
                return (Callable) callableGetMethod.invoke(null, task);
            } catch (Exception e) {
                //ignore exception
            }
        }
        return task;
    }

    private static <T> Callable<T> CatWrapper(Callable<T> task) {
//        return CatTraceCallable.get(task);
        return task;
    }

    private static <T> Callable<T> RhinoWrapper(Callable<T> origin, Callable<T> target, String rhinoKey) {
        return new RhinoProfilingCallable<T>(origin, target, rhinoKey);
    }

    /**
     * 兼容旧版
     *
     * @param task
     * @return
     */
    public static Runnable wrap(Runnable task) {
        Runnable finalTask = task;
        // 这里顺序不能改
        // 必须先封装cat，然后封装mtrace，否则cat埋点无法感知到压测标识
        finalTask = CatWrapper(finalTask);              //cat透传
        finalTask = MtraceWrapper(finalTask);           //mtrace透传
        return finalTask;
    }

    public static Runnable wrap(Runnable task, String rhinoKey) {
        Runnable finalTask = wrap(task);
        finalTask = RhinoWrapper(task, finalTask, rhinoKey);  //rhino监控与告警
        return finalTask;
    }

    public static Runnable wrap(Runnable task, String rhinoKey, boolean traceable) {
        Runnable finalTask = task;
        if (traceable) {
            finalTask = wrap(task);
        }
        finalTask = RhinoWrapper(task, finalTask, rhinoKey);  //rhino监控与告警
        return finalTask;
    }

    private static Runnable MtraceWrapper(Runnable task) {
        if (runnableGetMethod != null) {
            try {
                return (Runnable) runnableGetMethod.invoke(null, task);
            } catch (Exception e) {
                //ignore exception
            }
        }
        return task;
    }

    private static Runnable CatWrapper(Runnable task) {
//        return CatTraceRunnable.get(task);
        return task;
    }

    private static Runnable RhinoWrapper(Runnable orgin, Runnable target, String rhinoKey) {
        return new RhinoProfilingRunnable(orgin, target, rhinoKey);
    }
}
