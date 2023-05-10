package src.juc.pratice.limit;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 20个并发，访问次数不超过10000
 * 限流
 * @author caoyang
 * @create 2023-05-06 11:22
 */
@Slf4j(topic = "limit")
public class Limit {

    private final int totalRequests;
    private ExecutorService executorService;
    private static final Object lock = new Object();
    private int count;

    public Limit(int workersNum, int totalRequests) {
        this.totalRequests = totalRequests;
        this.executorService = Executors.newFixedThreadPool(workersNum);
        this.count = 0;
    }

    public void execute(){
        synchronized (lock){
            if (count < totalRequests){
                executorService.submit(new Task());
                count++;
            }
        }
    }

    public static void main(String[] args) {
        Limit limit = new Limit(5, 10);
        while (true){
            limit.execute();
        }
    }

    static class Task implements Runnable{
        @Override
        public void run() {
           log.info("execute task");
        }
    }



}
