package src.juc.lock;

import lombok.extern.slf4j.Slf4j;
import org.openjdk.jol.info.ClassLayout;

/**
 * 其他线程竞争导致偏向锁撤销
 * @author caoyang
 */
@Slf4j(topic = "TestBiasDismiss2")
public class TestBiasDismiss2 {
    public static void main(String[] args) throws InterruptedException {
        TestBiasDismiss2 test = new TestBiasDismiss2();
        log.debug(ClassLayout.parseInstance(test).toPrintable());
        Thread t1 = new Thread(() -> {
            log.debug("begin");
            synchronized (test){
                log.debug(ClassLayout.parseInstance(test).toPrintable());
            }
            synchronized (TestBiasDismiss2.class){
                TestBiasDismiss2.class.notify();
            }
        },"t1");

        Thread t2 = new Thread(() -> {
            log.debug("begin");
            synchronized (TestBiasDismiss2.class){
                try {
                    TestBiasDismiss2.class.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            synchronized (test){
                log.debug(ClassLayout.parseInstance(test).toPrintable());
            }
        },"t2");

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        log.debug(ClassLayout.parseInstance(test).toPrintable());
    }
}
