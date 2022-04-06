package src.juc.pratice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 生产者消费者队列
 * @author caoyang
 */
public class TestConsumerProducer2<T> {
    private static int size = 0;
    final static int MAX = 10;
    final static int CONSUMER_NUM = 10;
    final static int PRODUCER_NUM = 2;
    List<T> lists = new ArrayList<>();
    public synchronized void put(T t){
        while (lists.size() == MAX){
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        lists.add(t);
        size++;
        // 唤醒所有线程，无法精确到消费者线程
        this.notifyAll();
    }

    public synchronized T get(){
        T t = null;
        while (lists.size() == 0){
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        t = lists.remove(0);
        size--;
        // 唤醒所有线程，无法精确到生产者线程
        this.notifyAll();
        return t;
    }

    public synchronized int getCount(){
        return size;
    }

    public static void main(String[] args) {
        TestConsumerProducer2<String> test = new TestConsumerProducer2();

        //启动生产者线程
        for (int i = 0; i < PRODUCER_NUM; i++) {
            new Thread(() -> {
                String s = "money";
                while (true){
                    test.put(s);
                    System.out.println(Thread.currentThread().getName() + " [put] " + s + "，存有 " + test.getCount());
                }
            }, "Producer"+i).start();
        }
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 启动消费者线程
        for (int i = 0; i < CONSUMER_NUM; i++) {
            new Thread(() -> {
                while (true){
                    String s = test.get();
                    System.out.println(Thread.currentThread().getName()+" [get] "+ s + "剩余 " + test.getCount());
                }
            }, "Consumer"+i).start();
        }
    }

}
