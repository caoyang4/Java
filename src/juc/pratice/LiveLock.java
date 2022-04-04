package src.juc.pratice;

import lombok.extern.slf4j.Slf4j;
import src.juc.JucUtils;

/**
 * 活锁
 * 增加随机睡眠时间，避免活锁产生
 * @author caoyang
 */
@Slf4j(topic = "LiveLock")
public class LiveLock {
    static int count = 10;
    public static void main(String[] args) {
        new Thread(() -> {
            while (count > 0) {
                JucUtils.sleepMillSeconds(200);
                count--;
                log.info("count: {}", count);
            }
        }).start();
        new Thread(() -> {
            while (count < 20) {
                JucUtils.sleepMillSeconds(200);
                count++;
                log.info("count: {}", count);
            }
        }).start();
    }
}
