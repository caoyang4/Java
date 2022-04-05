package src.juc.atomic;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;

/**
 * @author caoyang
 */
@Slf4j(topic = "TestAtomicUpdate")
public class TestAtomicUpdate {
    AtomicInteger value;

    public TestAtomicUpdate() {
        this.value = new AtomicInteger(9);
    }

    /**
     *
     * @param operator 操作器
     * @return
     */
    public int selfUpdateAndGet(IntUnaryOperator operator){
        while (true) {
            int val1 = value.get();
            int val2 = operator.applyAsInt(val1);
            if (value.compareAndSet(val1, val2)){
                return value.get();
            }
        }
    }

    public static void main(String[] args) {
        AtomicInteger index = new AtomicInteger(1);
        index.incrementAndGet();
        log.info("incrementAndGet: {}", index);
        index.getAndIncrement();
        log.info("getAndIncrement: {}", index);
        index.getAndAdd(2);
        log.info("getAndAdd: {}", index);
        index.updateAndGet(value -> value * 10);
        log.info("updateAndGet: {}", index);

        System.out.println("==================");
        TestAtomicUpdate test = new TestAtomicUpdate();
        test.selfUpdateAndGet(value -> value*value+1);
        log.info("selfUpdateAndGet: {}", test.value.get());


    }
}
