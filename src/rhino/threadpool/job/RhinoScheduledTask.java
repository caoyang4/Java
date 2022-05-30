package src.rhino.threadpool.job;

import java.util.concurrent.TimeUnit;

/**
 * Created by zmz on 2020/11/16.
 */
public interface RhinoScheduledTask extends Runnable {
    String name();
    long initialDelay();    //初次任务延迟
    long period();          //循环执行周期
    TimeUnit periodUnit();  //循环执行周期单位
}
