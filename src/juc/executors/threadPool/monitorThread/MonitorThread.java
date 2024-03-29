package src.juc.executors.threadPool.monitorThread;

import java.util.concurrent.ThreadPoolExecutor;

public class MonitorThread implements Runnable{
    private ThreadPoolExecutor executor;
    private boolean run = true;
    private int seconds;

    public MonitorThread(ThreadPoolExecutor executor, int seconds) {
        this.executor = executor;
        this.seconds = seconds;
    }

    public void setRun(boolean run) {
        this.run = run;
    }

    public void shutdown(){
        setRun(false);
    }

    @Override
    public void run() {
        while(run){
            System.out.println(
                    String.format("[monitor] [%d/%d] Active: %d, Completed: %d, Task: %d, isShutdown: %s, isTerminated: %s",
                            this.executor.getPoolSize(),
                            this.executor.getCorePoolSize(),
                            this.executor.getActiveCount(),
                            this.executor.getCompletedTaskCount(),
                            this.executor.getTaskCount(),
                            this.executor.isShutdown(),
                            this.executor.isTerminated()));
            try {
                Thread.sleep(seconds*1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
