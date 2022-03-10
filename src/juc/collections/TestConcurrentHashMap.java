package src.juc.collections;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Hashtable，synchronizedHashMap，ConcurrentHashMap
 * @author caoyang
 */
public class TestConcurrentHashMap {
    public static void main(String[] args) throws InterruptedException {
        Map<String, Object> hashtable = new Hashtable<>();
        Map<String, Object> synchronizedMap = Collections.synchronizedMap(new HashMap<>());
        Map<String, Object> concurrentHashMap = new ConcurrentHashMap<>();
        TestConcurrentHashMap test = new TestConcurrentHashMap();
        System.out.println("hashtable cost " + test.timeAvgCostForGetPut(hashtable));
        System.out.println("synchronizedMap cost " + test.timeAvgCostForGetPut(synchronizedMap));
        System.out.println("concurrentHashMap cost " + test.timeAvgCostForGetPut(concurrentHashMap));

    }

    public long timeAvgCostForGetPut(Map<String, Object> map) throws InterruptedException {
        final int times = 1000000;
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        long start = System.nanoTime();
        for (int i = 0; i < 10; i++) {
            executorService.execute(() -> {
                for (int j = 0; j < times; j++) {
                    int num = ThreadLocalRandom.current().nextInt(10000);
                    String key = String.valueOf(num);
                    map.put(key, new Object());
                    map.get(key);
                }
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(15, TimeUnit.SECONDS);
        return (System.nanoTime() - start)/times;
    }
}
