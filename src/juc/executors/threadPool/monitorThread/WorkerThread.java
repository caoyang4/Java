package src.juc.executors.threadPool.monitorThread;

import java.util.Date;

/**
 * @author caoyang
 */
public class WorkerThread implements Runnable{
    private String command;

    public WorkerThread(String command) {
        this.command = command;
    }

    @Override
    public void run() {
        doWork();
    }

    private void doWork(){
        try {
            System.out.println(Thread.currentThread().getName() + " start to do work, command=" + command + " " + new Date());
            Thread.sleep(1000);
            System.out.println(Thread.currentThread().getName() + " end to do work, command=" + command + " " + new Date());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "WorkerThread{" +
                "command='" + command + '\'' +
                '}';
    }
}
