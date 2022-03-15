package src.juc.lock;

import java.util.ArrayList;
import java.util.List;

/**
 *  * 实现一个容器，提供两个方法，add，size
 *  * 写两个线程，线程1添加10个元素到容器中，线程2实现监控元素的个数，当个数到5个时，线程2给出提示并结束
 *
 * @author caoyang
 */
public class TestWaitNotify3 {
    private List<Object> list;

    public TestWaitNotify3(List<Object> list) {
        this.list = list;
    }

    public void add(Object obj){
        list.add(obj);
    }
    public int size(){
        return list.size();
    }

    public static void main(String[] args) {
        TestWaitNotify3 test = new TestWaitNotify3(new ArrayList<>());
        Object lock = new Object();
        Thread t1 = new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + " start...");
            synchronized (lock) {
                for (int i = 0; i < 9; i++) {
                    test.add(new Object());
                    System.out.println(Thread.currentThread().getName() + " add obj" + i);
                    if(test.size() == 5){
                        // 唤醒线程2，但并不释放锁
                        lock.notify();
                        try {
                            // 释放锁
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            System.out.println(Thread.currentThread().getName() + " end...");
        }, "t1");

        Thread t2 = new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + " start...");
            synchronized (lock) {
                if(test.size() != 5){
                    try {
                        // 释放锁
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                // 唤醒线程t1
                lock.notify();
            }
            System.out.println(Thread.currentThread().getName() + " end...");
        }, "t2");

        t2.start();
        t1.start();
    }
}
