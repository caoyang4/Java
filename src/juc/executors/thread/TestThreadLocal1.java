package src.juc.executors.thread;

/**
 * ThreadLocal 类来实现线程本地存储功能
 * 应用最多的是session管理和数据库链接管理
 * thread1 中设置 threadLocal 为 1，而 thread2 设置 threadLocal 为 2。
 * 过了一段时间之后，thread1 读取 threadLocal 依然是 1，不受 thread2 的影响
 *
 *
 * @author caoyang
 */
public class TestThreadLocal1 {
    public static void main(String[] args) {
        ThreadLocal local = new ThreadLocal();
        new Thread(() -> {
            local.set(1);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(local.get());
            local.remove();
        }).start();
        new Thread(() -> {
            local.set(2);
            local.remove();
        }).start();
    }
}
