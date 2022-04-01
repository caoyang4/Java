package src.juc.executors.thread;

/**
 * 线程优先级
 * @author caoyang
 */
public class TestPriority {
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
                System.out.println("---->---->" + count++);
            }
        });

        t1.setPriority(Thread.MIN_PRIORITY);
        t1.setPriority(Thread.MAX_PRIORITY);
        t1.start();
        t2.start();
    }
}
