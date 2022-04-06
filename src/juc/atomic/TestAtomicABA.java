package src.juc.atomic;

import lombok.extern.slf4j.Slf4j;
import src.juc.JucUtils;

import java.util.concurrent.atomic.AtomicReference;

/**
 * ABA问题
 * @author caoyang
 */
@Slf4j(topic = "TestAtomicABA")
public class TestAtomicABA {
    static AtomicReference<String> ref = new AtomicReference<>("A");
    public static void other(){
        new Thread(() -> {
            ref.compareAndSet("A", "B");
            log.info("ref[A->B]: {}", ref.get());
        }, "t1").start();
        JucUtils.sleepSeconds(1);
        new Thread(() -> {
            ref.compareAndSet("B", "A");
            log.info("ref[B->A]: {}", ref.get());
        }, "t2").start();
    }

    public static void main(String[] args) {
        other();
        new Thread(() -> {
            JucUtils.sleepSeconds(2);
            ref.compareAndSet("A", "C");
        }, "t0").start();
        JucUtils.sleepSeconds(4);
        log.info("ref[A->C]: {}", ref.get());
    }


}
