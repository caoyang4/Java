package src.juc.pratice;

import lombok.extern.slf4j.Slf4j;

/**
 * 死锁
 * @author caoyang
 */
@Slf4j(topic = "DeadLock")
public class DeadLock {
    public static void main(String[] args) {
        Object obj1 = new Object();
        Object obj2 = new Object();
        new Thread(() -> {
            synchronized (obj1){
                log.info("get lock: obj1");
                synchronized (obj2){
                    log.info("get lock: obj2");
                }
            }
        }, "t1").start();

        new Thread(() -> {
            synchronized (obj2){
                log.info("get lock: obj2");
                synchronized (obj1){
                    log.info("get lock: obj1");
                }
            }
        }, "t2").start();
    }
}
