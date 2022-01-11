package juc.pratice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 维护consumer producer两个等待队列
 * @author caoyang
 */
public class TestCondition<T> {
    private static int size = 0;
    final static int MAX = 1000;
    final static int CONSUMER_NUM = 10;
    final static int PRODUCER_NUM = 2;
    List<T> lists = new ArrayList<>();
    ReentrantLock lock = new ReentrantLock();
    Condition consumer = lock.newCondition();
    Condition producer = lock.newCondition();

    public void put(T t){
        lock.lock();
        try {
            while (size == MAX) {
                producer.await();
            }
            lists.add(t);
            size++;
            consumer.signalAll();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public T get(){
        T t = null;
        lock.lock();
        try {
            while (size == 0) {
                consumer.await();
            }
            t = lists.remove(0);
            size--;
            producer.signalAll();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return t;
    }

    public int getCount(){
        int count = 0;
        lock.lock();
        try{
            count = size;
        } finally {
            lock.unlock();
        }
        return count;
    }

    public static void main(String[] args) {
        TestCondition<String> test = new TestCondition();
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
