package src.juc.executors.threadPool.selfThreadPool;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Future;

/**
 * 抽象线程池
 * @author caoyang
 */
public abstract class AbstractThreadPool implements ThreadPoolInterface{

    /**
     * 反射提交任务,不带参数
     * @param o
     * @param methodName
     * @return
     */
    @Override
    public Future<?> submit(Object o, String methodName) {
        return submit(constructTask(o, methodName));
    }

    /**
     * 反射执行任务,不带参数
     * @param o
     * @param methodName
     * @return
     */
    @Override
    public void execute(Object o, String methodName) {
        execute(constructTask(o, methodName));
    }

    @Override
    public boolean cancelTask(Future<?> future) {
        return future.isCancelled() || future.cancel(true);
    }

    private Runnable constructTask(final Object o, final String methodName){
        return () -> {
            try {
                Method method = o.getClass().getMethod(methodName);
                method.invoke(o);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        };
    }
}
