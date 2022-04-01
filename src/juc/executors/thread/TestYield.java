package src.juc.executors.thread;

/**
 * yield 让线程从 running 到 runnable，然后执行其他同优先级线程
 * 若此时没有同优先级线程，不会有暂停效果
 * @author caoyang
 */
public class TestYield {
    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
            int count = 0;
            while (true){
                System.out.println("---->" + count++);
            }
        });
        Thread t2 = new Thread(() -> {
            int count = 0;
            while (true){
                Thread.yield();
                System.out.println("---->---->" + count++);
            }
        });
        t1.start();
        t2.start();
    }
}
