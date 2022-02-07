package src.juc.thread.threadLocalOOM;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * - ThreadLocal 被回收
 * - 线程被复用
 * - 线程复用后未调用 ThreadLocal 的 set/get/remove 方法
 */

public class MyThreadLocalOOM {
    public static final Integer SIZE = 500;
    static ThreadPoolExecutor executor = new ThreadPoolExecutor(
            5, 5, 1,
            TimeUnit.MINUTES, new LinkedBlockingDeque<>());

    static class LocalVariable {//总共有5M
        private byte[] bytes = new byte[1024 * 1024 * 5];
    }

    static ThreadLocal<LocalVariable> local = new ThreadLocal<>();

    public static void revealTest(){
        try {
            for (int i = 0; i < SIZE; i++) {
                executor.execute(() -> {
                    local.set(new LocalVariable());
                    System.out.println("开始执行");
                });
                Thread.sleep(100);
            }
            // local设置为null，依旧会造成内存泄漏
            local = null;
            System.gc();
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void recycleTest(){
        try {
            for (int i = 0; i < SIZE; i++) {
                executor.execute(() -> {
                    local.set(new LocalVariable());
                    System.out.println("开始执行");
                    // 调用local.remove()将threadLocal内的对象删除
                    local.remove();
                });
                Thread.sleep(100);
            }
            System.gc();
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // 泄漏
        revealTest();

        // 回收
        // recycleTest();
    }
}
