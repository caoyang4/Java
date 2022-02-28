package src.juc.atomic;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author caoyang
 */
public class TestAtomicInteger {
    private AtomicInteger atomic = new AtomicInteger(0);


    public int increase(){
        return atomic.incrementAndGet();
    }
    public int get(){
        return atomic.get();
    }
    public static void main(String[] args) {
        TestAtomicInteger test = new TestAtomicInteger();
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    test.increase();
                }
                System.out.println(Thread.currentThread().getName() + " result: "+test.get());
            }, "thread"+i).start();
        }
    }
}
