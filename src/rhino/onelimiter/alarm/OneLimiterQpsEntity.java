package src.rhino.onelimiter.alarm;

/**
 * Created by zhen on 2018/12/3.
 */
public class OneLimiterQpsEntity {

    private long time;
    private int successCount;
    private int warnCount;
    private int rejectCount;

    public OneLimiterQpsEntity() {
    }

    public OneLimiterQpsEntity(long time, int successCount, int warnCount, int rejectCount) {
        this.time = time;
        this.successCount = successCount;
        this.warnCount = warnCount;
        this.rejectCount = rejectCount;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public long getWarnCount() {
        return warnCount;
    }

    public void setWarnCount(int warnCount) {
        this.warnCount = warnCount;
    }

    public long getRejectCount() {
        return rejectCount;
    }

    public void setRejectCount(int rejectCount) {
        this.rejectCount = rejectCount;
    }
}
