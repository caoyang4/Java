package src.juc.lock;

import lombok.extern.slf4j.Slf4j;
import src.juc.JucUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * CountDawnLatch+线程池实现任务协作同步
 * @author caoyang
 */
@Slf4j(topic = "TestCountDawnLatch3")
public class TestCountDawnLatch3 {
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch countDownLatch = new CountDownLatch(4);
        executorService.submit(() -> {
            log.info("[1]begin...");
            countDownLatch.countDown();
            JucUtils.sleepSeconds(2);
            log.info("[1]end...");
        });
        executorService.submit(() -> {
            log.info("[2]begin...");
            countDownLatch.countDown();
            JucUtils.sleepSeconds(1);
            log.info("[2]end...");
        });
        executorService.submit(() -> {
            log.info("[3]begin...");
            JucUtils.sleepSeconds(2);
            countDownLatch.countDown();
            log.info("[3]end...");
        });
        executorService.submit(() -> {
            log.info("[4]begin...");
            JucUtils.sleepSeconds(1);
            countDownLatch.countDown();
            log.info("[4]end...");
        });
        executorService.submit(() -> {
            log.info("await...");
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("await end...");
        });


    }
}
