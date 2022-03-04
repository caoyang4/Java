package src.juc.lock;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author caoyang
 */
public class TestWaitNotify2 {

    //等待列表, 用来记录等待的顺序
    private static List<String> waitList = new LinkedList<>();
    //唤醒列表, 用来唤醒的顺序
    private static List<String> notifyList = new LinkedList<>();

    private static Object lock = new Object();

    public static void main(String[] args) throws InterruptedException {
        //创建50个线程
        for(int i=0; i<50; i++){
            new Thread(() -> {
                synchronized (lock) {
                    String threadName = Thread.currentThread().getName();
                    System.out.println("线程 ["+threadName+"] wait...");
                    waitList.add(threadName);
                    try {
                        // lock.wait后 被通知到的线程，就会进入waitSet队列;
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("线程 ["+threadName+"] notify...");
                    notifyList.add(threadName);
                }
            }, Integer.toString(i)).start();
            TimeUnit.MILLISECONDS.sleep(50);
        }

        TimeUnit.SECONDS.sleep(1);

        for(int i=0; i<50; i++){
            synchronized (lock) {
                // hotspot对notify()的实现并不是随机唤醒, 而是“先进先出”的顺序唤醒
                lock.notify();
            }
            // 若此处没有sleep，会发生锁竞争，导致唤醒乱序
            // 有sleep之后，无锁竞争，唤醒线程顺序执行
            TimeUnit.MILLISECONDS.sleep(10);
        }
        System.out.println("wait 顺序: "+waitList);
        System.out.println("notify 顺序: "+notifyList);

    }

}
