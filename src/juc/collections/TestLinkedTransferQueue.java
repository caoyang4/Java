package src.juc.collections;

import lombok.extern.slf4j.Slf4j;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;

/**
 * LinkedTransferQueue实现了线程间的数据交换
 * take() 从队列中获取数据，如果没有数据阻塞，获取队列中第一个元素
 * put() 向队列中添加数据（队列尾部），加锁，线程安全的
 */
@Slf4j(topic = "TestLinkedTransferQueue")
public class TestLinkedTransferQueue {
    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<Integer> q = new LinkedTransferQueue();
        for (int i = 0; i < 10; i++) {
            new Thread(()-> {
                try {
                    Object take = q.take();
                    log.info("线程消费:{}",take);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            },"consumer"+i).start();
        }
        for (int i = 0; i < 10; i++) {
            TimeUnit.SECONDS.sleep(1);
            new Thread(()-> {
                try {
                    int num = new Random().nextInt(100);
                    log.info("线程生产:{}",num);
                    q.put(num);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            },"producer"+i).start();
        }

    }
}
