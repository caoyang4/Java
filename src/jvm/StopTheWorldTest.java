package src.jvm;

import lombok.extern.slf4j.Slf4j;
import src.juc.JucUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 执行 full gc，会暂停用户线程
 */
@Slf4j(topic = "StopTheWorldTest")
public class StopTheWorldTest {
    static class PrintThread extends Thread{
        public static final long start = System.currentTimeMillis();
        @Override
        public void run() {
            while (true) {
                long t = System.currentTimeMillis() - start;
                // 发生gc时，间隔时间如果不是1s，则表明存在STW
                log.info("{} ms" ,(t/1000 + "." + t % 1000));
                JucUtils.sleepSeconds(1);
            }
        }
    }

    static class WorkerThread extends Thread{
        List<Byte[]> list = new ArrayList<>();

        @Override
        public void run() {
            while (true) {
                for (int i = 0; i < 1000; i++) {
                    list.add(new Byte[1024]);
                }
                if (list.size() > 10000){
                    list.clear();
                    System.gc();
                }
            }
        }
    }

    public static void main(String[] args) {
        Thread thread1 = new PrintThread();
        Thread thread2 = new WorkerThread();
        thread1.start();
        thread2.start();
    }
}
