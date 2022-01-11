package src.juc;

import java.util.concurrent.Exchanger;

/**
 * 场景：游戏中交换装备
 * @author caoyang
 */
public class TestExchanger {
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
