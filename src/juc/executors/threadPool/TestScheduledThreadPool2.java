package src.juc.executors.threadPool;

import lombok.extern.slf4j.Slf4j;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 定时任务
 * @author caoyang
 */
@Slf4j(topic = "TestScheduledThreadPool2")
public class TestScheduledThreadPool2 {
    public static void main(String[] args) {
        // 每周一 18：30 定时执行
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
        // 间隔一周
        long interval = 1000 * 60 * 60 * 24 * 7;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime initialDelay = now.withHour(18).withMinute(30).withSecond(0).withNano(0).with(DayOfWeek.MONDAY);

        if (now.compareTo(initialDelay) > 0){
            initialDelay = initialDelay.plusWeeks(1);
        }
        long timeWait = Duration.between(now, initialDelay).toMillis();
        executorService.scheduleWithFixedDelay(
                ()->{log.info("execute timed task");},
                timeWait,
                interval,
                TimeUnit.MILLISECONDS
        );
    }
}
