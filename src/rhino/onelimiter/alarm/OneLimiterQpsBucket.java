package src.rhino.onelimiter.alarm;

import java.util.concurrent.atomic.AtomicLong;


/**
 * Created by zhen on 2018/11/28.
 */
public class OneLimiterQpsBucket {

    public AtomicLong successCount = new AtomicLong();
    public AtomicLong rejectCount = new AtomicLong();
    public AtomicLong warnCount = new AtomicLong();

    public long markSuccess(int tokenNum) {
        return successCount.addAndGet(tokenNum);
    }

    public void markWarn(int tokenNum) {
        warnCount.addAndGet(tokenNum);
    }

    public void markReject(int tokenNum) {
        rejectCount.addAndGet(tokenNum);
    }

    public void clear() {
        successCount.set(0);
        rejectCount.set(0);
        warnCount.set(0);
    }

    public OneLimiterQpsEntity asEntity(long time) {
        return new OneLimiterQpsEntity(time, successCount.intValue(), warnCount.intValue(), rejectCount.intValue());
    }

    @Override
    public String toString() {
        return "OneLimiterQpsBucket{" +
                "successCount=" + successCount +
                ", rejectCount=" + rejectCount +
                ", warnCount=" + warnCount +
                '}';
    }
}


