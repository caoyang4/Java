package src.juc.lock;

import java.util.concurrent.Exchanger;

/**
 * Exchanger是一种线程间安全交换数据的机制
 * SynchronousQueue是交给一个数据，Exchanger是交换两个数据
 * 场景：游戏中交换装备
 *
 * 有2条线程A和B，A线程交换数据时，发现slot为空，则将需要交换的数据放在slot中等待其它线程进来交换数据，
 * 等线程B进来，读取A设置的数据，然后设置线程B需要交换的数据，然后唤醒A线程
 * @author caoyang
 */
public class TestExchanger1 {
    public static void main(String[] args) {
        // 容器
        Exchanger<String> exchanger = new Exchanger<>();
        new Thread(() -> {
            String s = "T1";
            try {
                s = exchanger.exchange(s);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName()+ " -> s:" + s);
        },"thread1").start();
        new Thread(() -> {
            String s = "T2";
            try {
                s = exchanger.exchange(s);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName()+ " -> s:" + s);
        }, "thread2").start();
    }
}
