package src.juc.executors.threadPool;

import lombok.extern.slf4j.Slf4j;
import src.juc.JucUtils;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author caoyang
 */
@Slf4j(topic = "TestTimer")
public class TestTimer {
    public static void main(String[] args) {
        // 出现异常，会影响其他任务执行，已经不推荐使用
        Timer timer = new Timer();
        TimerTask task1 = new TimerTask() {
            @Override
            public void run() {
                log.info("execute task1");
            }
        };
        TimerTask task2 = new TimerTask() {
            @Override
            public void run() {
                JucUtils.sleepSeconds(2);
                log.info("execute task2");
            }
        };
        log.info("main start");
        timer.schedule(task2, 1000);
        timer.schedule(task1, 1000);
    }
}
