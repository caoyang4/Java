package src.jvm;

import src.juc.JucUtils;

/**
 * 类加载死锁
 * @author caoyang
 * @create 2022-05-23 23:09
 */
public class LoadDeadLock {
    static class A{
        // 加载 A，A 的 clinit 会加锁
        static {
            System.out.println("begin to init A");
            JucUtils.sleepSeconds(1);
            // 加载 B，需要执行 B 的 clinit，需要先获取锁
            new B();
            System.out.println("end to init A");
        }
    }
    static class B{
        static {
            System.out.println("begin to init B");
            JucUtils.sleepSeconds(1);
            new A();
            System.out.println("end to init B");
        }
    }
    // 加载 A 和 B 会形成死锁
    public static void main(String[] args) {
        new Thread(() -> {
            new A();
        }, "threadA").start();

        new Thread(() -> {
            new B();
        }, "threadB").start();
    }
}
