package src.juc.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * CachedThreadPool: 一个任务创建一个线程；
 * FixedThreadPool: 所有任务只能使用固定大小的线程；
 * SingleThreadExecutor: 相当于大小为 1 的 FixedThreadPool
 * @author caoyang
 */
public class TestExecutor {
    public static void main(String[] args) {
        Example e1 = new Example();
        Example e2 = new Example();
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.execute(() -> e1.func());
        executorService.execute(() -> e2.func());
    }
}

class Example{
    public void func(){
        synchronized (this){
            for (int i = 0; i < 10; i++) {
                System.out.print(i + "\t");
            }
        }
    }
}
