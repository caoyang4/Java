package container;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author caoyang
 */
public class TestWaitNotify {
    List<Object> objects = new ArrayList<>();
    final Object lock = new Object();

    public static void main(String[] args) {
        TestWaitNotify test = new TestWaitNotify();
        new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + "线程启动...");
            synchronized (test.lock){
                if(test.objects.size() != 5){
                    try {
                        System.out.println("t2 让出锁");
                        test.lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println(Thread.currentThread().getName() + "线程结束...");
                // 通知 t1 继续执行
                test.lock.notify();
                System.out.println("t2 通知 t1 继续执行");
            }
        }, "Thread2").start();

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        new Thread(() -> {
            synchronized (test.lock){
                System.out.println(Thread.currentThread().getName() + "线程启动...");
                for (int i = 0; i < 10; i++) {
                    System.out.println("objects添加object"+i);
                    test.objects.add(new Object());
                    if(test.objects.size() == 5){
                        // 先通知t2，再让出锁
                        System.out.println("t1 通知 t2 继续执行");
                        test.lock.notify();
                        try {
                            // 让出锁，阻塞
                            System.out.println("t1 让出锁");
                            test.lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                System.out.println(Thread.currentThread().getName() + "线程结束...");
            }
        },"Thread1").start();
    }
}
