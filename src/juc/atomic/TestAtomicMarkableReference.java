package src.juc.atomic;

import lombok.extern.slf4j.Slf4j;
import src.juc.JucUtils;

import java.util.concurrent.atomic.AtomicMarkableReference;

/**
 * @author caoyang
 */
@Slf4j(topic = "TestAtomicMarkableReference")
public class TestAtomicMarkableReference {
    public static void main(String[] args) {
        Object obj = new Object();
        AtomicMarkableReference<Object> ref = new AtomicMarkableReference(obj, true);
        new Thread(() -> {
            boolean isSuccess = ref.compareAndSet(ref.getReference(), new Object(), true, false);
            log.info("isSuccess:{}", isSuccess);
        },"t1").start();
        new Thread(() -> {
            boolean isSuccess = ref.compareAndSet(ref.getReference(), new Object(), true, false);
            log.info("isSuccess:{}", isSuccess);
        },"t2").start();

    }

}
