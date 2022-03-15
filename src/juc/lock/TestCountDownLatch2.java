package src.juc.lock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 实现一个容器，提供两个方法，add，size
 * 写两个线程，线程1添加10个元素到容器中，线程2实现监控元素的个数，当个数到5个时，线程2给出提示并结束
 *
 * @author caoyang
 */
public class TestCountDownLatch2 {
    private List<Object> list;

    public TestCountDownLatch2(List<Object> list) {
        this.list = list;
    }

    public void add(Object obj){
        list.add(obj);
    }
    public int size(){
        return list.size();
    }

    public static void main(String[] args) {
        TestCountDownLatch2 test = new TestCountDownLatch2(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(1);
        Thread t1 = new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + " start...");
            for (int i = 0; i < 10; i++) {
                test.add(new Object());
                System.out.println(Thread.currentThread().getName() + " add obj" + i);
                if(test.size() == 5){
                    latch.countDown();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            System.out.println(Thread.currentThread().getName() + " end...");
        }, "t1");

        Thread t2 = new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + " start...");
            try {
                if(test.size() != 5) {
                    latch.await();
                }
                System.out.println(Thread.currentThread().getName() + " end...");
            } catch (InterruptedException e) {
                e.printStackTrace();

            }
        }, "t2");

        t2.start();
        t1.start();
    }
}
