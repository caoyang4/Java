package src.rhino.metric;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import src.rhino.util.LongAdder;

/**
 * @author zhanjun on 2017/4/23.
 */
public class HealthCount {

    private LongAdder successCount = new LongAdder();
    private LongAdder errorCount = new LongAdder();
    private ConcurrentHashMap<Class<? extends Throwable>, AtomicInteger> exceptionCount = new ConcurrentHashMap<>();

    public void clear() {
        errorCount.reset();
        successCount.reset();
        exceptionCount.clear();
    }

    /**
     * 统计异常总数和各个异常的数量
     * @param throwable
     */
    public void markFailed(Throwable throwable) {
        errorCount.increment();
        Class<? extends Throwable> errorType = throwable.getClass();
        AtomicInteger count = exceptionCount.get(errorType);
        if (count == null) {
            count = new AtomicInteger();
            AtomicInteger count0 = exceptionCount.putIfAbsent(errorType, count);
            if (count0 != null) {
                count = count0;
            }
        }
        count.incrementAndGet();
    }

    public void markSuccess() {
        successCount.increment();
    }

    public int getErrorCount() {
        return errorCount.intValue();
    }

    public int getSuccessCount() {
        return successCount.intValue();
    }

    public ConcurrentHashMap<Class<? extends Throwable>, AtomicInteger> getExceptionCount() {
        return exceptionCount;
    }
}
