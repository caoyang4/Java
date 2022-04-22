package src.juc.collections;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Hashtable，synchronizedHashMap，ConcurrentHashMap
 * 为什么ConcurrentHashMap以及Hashtable这样的同步容器不允许键值对为null呢？
 * 因为concurrenthashmap以及hashtable是用于多线程的，如果map.get(key)得到了null，不能判断到底是映射的value是null,
 * 还是因为 没有找到对应的key而为空，而用于单线程状态的hashmap却可以用containKey（key） 去判断到底是否包含了这个null。
 * ConcurrentHashMap为什么就不能containKey(key)？因为一个线程先get(key)再containKey(key)，
 * 这两个方法的中间时刻，其他线程怎么操作这个key都会可能发生，例如删掉这个key
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
