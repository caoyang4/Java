package src.guava;

import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author caoyang
 * @create 2024-02-01 10:43
 */
@Slf4j(topic = "GuavaRateLimiterTest")
public class GuavaRateLimiterTest {

    public static void main(String[] args) {
        // 1 permits per second
        RateLimiter rateLimiter = RateLimiter.create(1.0);
        List<Runnable> taskList = new ArrayList<>();
        int count = 1000;
        for (int i = 0; i < count; i++) {
            final int ctl = i + 1;
            taskList.add(() -> log.info("task{}",ctl));
        }
        ThreadPoolExecutor executor = new ThreadPoolExecutor(4, 4, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        submit(taskList, executor,rateLimiter);
    }

    public static void submit(List<Runnable> tasks, Executor executor, RateLimiter rateLimiter) {
        for (Runnable task : tasks) {
            rateLimiter.acquire();
            executor.execute(task);
        }
    }

}
