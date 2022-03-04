package src.juc.collections;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 单向链表队列
 * tail和head是延迟更新
 *
 * 如果让tail永远作为队列的队尾节点，实现的代码量会更少，而且逻辑更易懂。
 * 但是，这样做有一个缺点，如果大量的入队操作，每次都要执行CAS进行tail的更新，汇总起来对性能也会是大大的损耗。
 * 如果能减少CAS更新的操作，无疑可以大大提升入队的操作效率，
 * 所以每间隔1次(tail和队尾节点的距离为1)进行才利用CAS更新tail。对head的更新也是同样的道理
 *
 * @author caoyang
 */
public class TestConcurrentLinkedQueue {
    public static void main(String[] args) {
        ConcurrentLinkedQueue<Integer> clq = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueueThread thread1 = new ConcurrentLinkedQueueThread("AddThread", "add", clq);
        ConcurrentLinkedQueueThread thread2 = new ConcurrentLinkedQueueThread("PollThread1", "poll", clq);
        ConcurrentLinkedQueueThread thread3 = new ConcurrentLinkedQueueThread("PollThread2", "poll", clq);
        thread1.start();
        thread2.start();
        thread3.start();
    }
}

class ConcurrentLinkedQueueThread extends Thread{
    String type;
    ConcurrentLinkedQueue clq;
    public ConcurrentLinkedQueueThread(String name, String type, ConcurrentLinkedQueue clq) {
        super(name);
        this.type = type;
        this.clq = clq;
    }

    @Override
    public void run() {
        try {
            if ("add".equals(type)){
                add();
            } else if ("poll".equals(type)){
                poll();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void poll() throws InterruptedException {
        for (;;) {
            //  PollThread线程不会因为ConcurrentLinkedQueue队列为空而等待，而是直接返回null
            System.out.println(Thread.currentThread().getName() + " clq poll " + clq.poll());
            Thread.sleep(500);
        }
    }

    private void add() throws InterruptedException {
         for (int i = 0; ; i++) {
             clq.add(i);
             System.out.println(Thread.currentThread().getName() + " clq add " + i);
             Thread.sleep(500);
         }
     }
}