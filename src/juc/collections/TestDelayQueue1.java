package src.juc.collections;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * 延迟队列
 * @author caoyang
 */
public class TestDelayQueue1 {
    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<DelayElement> queue = new DelayQueue<>();
        queue.put(new DelayElement(System.currentTimeMillis()));
        System.out.println(queue.take());
    }
}

class DelayElement implements Delayed{
    private final long activeTime;

    public DelayElement(long activeTime) {
        this.activeTime = activeTime;
    }

    @Override
    public long getDelay(@NotNull TimeUnit unit) {
        return  unit.convert(activeTime-System.nanoTime(), TimeUnit.NANOSECONDS);
    }

    @Override
    public int compareTo(@NotNull Delayed o) {
        if (this == o) {
            return 0;
        }
        long diff = getDelay(TimeUnit.NANOSECONDS) - o.getDelay(TimeUnit.NANOSECONDS);
        return diff == 0 ? 0 : (diff > 0 ? 1 : -1);
    }
}
