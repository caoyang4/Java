package src.rhino.threadpool.component;

import src.rhino.threadpool.RhinoThreadPoolExecutor;
import src.rhino.threadpool.ThreadPool;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池的各种关闭策略
 * Created by zmz on 2021/3/16.
 */
public enum ShutdownPolicy {
    //立即关闭并中断当前正在执行的任务，注意对于不响应中断的线程是无法关闭的
    SHUTDOWN_NOW{
        @Override
        public void shutdown(ThreadPoolExecutor executor) throws InterruptedException {
            executor.shutdownNow();
        }
    },

    //优雅关闭，立即拒绝提交新的任务，然后等待直到线程池将当前正在执行的任务和队列中的任务处理完成
    SHUTDOWN_GRACEFULLY{
        @Override
        public void shutdown(ThreadPoolExecutor executor) throws InterruptedException {
            executor.shutdown();
            while(!executor.awaitTermination(30, TimeUnit.SECONDS)){
                //继续等待直到确定关闭
            }
        }
    },

    //不关闭
    DO_NOTHING{
        @Override
        public void shutdown(ThreadPoolExecutor executor) {
            //do nothing
        }
    };

    public abstract void shutdown(ThreadPoolExecutor executor) throws InterruptedException;
}
