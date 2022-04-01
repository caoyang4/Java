package src.juc.executors.thread;

import lombok.extern.slf4j.Slf4j;
import src.juc.JucUtils;

import java.util.concurrent.TimeUnit;

/**
 * java层面的线程6种状态
 * @author caoyang
 */
@Slf4j
public class TestThreadState {
    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
            System.out.println("t1");
        });

        Thread t2 = new Thread(() -> {
            while (true) {

            }
        });
        t2.start();

        Thread t3 = new Thread(() -> {
            synchronized (TestThreadState.class){
                JucUtils.sleepSeconds(100);
            }
        });
        t3.start();

        Thread t4 = new Thread(() -> {
            try {
                // 无限等待 t2 结束
                t2.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        t4.start();

        Thread t5 = new Thread(() -> {
            // t5 获取不到锁，阻塞
            synchronized (TestThreadState.class){

            }
        });
        t5.start();

        // t6 正常跑完
        Thread t6 = new Thread(() -> {

        });
        t6.start();

        log.info("t1: "+t1.getState());
        log.info("t2: "+t2.getState());
        log.info("t3: "+t3.getState());
        log.info("t4: "+t4.getState());
        log.info("t5: "+t5.getState());
        log.info("t6: "+t6.getState());
    }
}
