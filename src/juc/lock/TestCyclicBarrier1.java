package src.juc.lock;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import static java.lang.System.*;

/**
 * CyclicBarrier
 * 栅栏
 * @author caoyang
 */
public class TestCyclicBarrier1 {
    public static void main(String[] args) {
        CyclicBarrier barrier = new CyclicBarrier(
                20,
                // 相当于钩子方法
                () -> out.println("满人，发车！！！")
        );

        for(int i=0; i<100; i++){
            new Thread(() -> {
                try {
                    barrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

}
