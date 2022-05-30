package src.rhino.limit.feature;

import java.util.concurrent.TimeUnit;

/**
 * Created by zhanjun on 2017/08/21.
 */
public class LimiterStrategyParams {

    private TimeUnit timeUnit;
    private int duration;
    private int count;

    public LimiterStrategyParams() {
    }

    public LimiterStrategyParams(int duration, int count) {
        this.duration = duration;
        this.count = count;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }
}
