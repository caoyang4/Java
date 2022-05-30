package src.juc.lock;

import lombok.extern.slf4j.Slf4j;
import org.openjdk.jol.info.ClassLayout;

/**
 * 其他线程竞争导致偏向锁撤销
 * 当调用锁对象的Object#hashcode()或System.identityHashCode()方法会导致该对象的偏向锁或轻量级锁升级
 * 如果是无锁状态则存放在mark word中，如果是重量级锁则存放在对应的monitor中，而偏向锁是没有地方能存放该信息的，所以必须升级
 *   当对象可偏向时，MarkWord将变成未锁定状态，并只能升级成轻量锁；
 *   当对象正处于偏向锁时，调用HashCode将使偏向锁强制升级成重量锁。
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
