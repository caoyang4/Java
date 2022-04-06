package src.juc.executors.threadPool.SelfRealizedPool;

import lombok.extern.slf4j.Slf4j;
import src.juc.JucUtils;

import java.util.concurrent.TimeUnit;

/**
 * @author caoyang
 */
@Slf4j(topic = "MainTest")
public class MainTest {
    public static void main(String[] args) {
        SelfExecutor pool = new SelfExecutor(2, 10,
                1000, TimeUnit.MILLISECONDS,
                (queue, task) -> {queue.offer(task, 500, TimeUnit.MILLISECONDS);});
        for (int i = 0; i < 15; i++) {
            int j = i;
            pool.execute(() -> {
                log.info("submit task {}", j+1);
                JucUtils.sleepSeconds(1);
            });
        }
    }
}
