
package src.rhino.limit;
/**
 * Created by wanghao on 18/1/31.
 */
public class RequestLimiterBean {
    private boolean isActive;
    private int rate;
    private int strategy;
    private long timeoutInMilliseconds;

    public RequestLimiterBean() {
        isActive = RequestLimiterProperties.default_isActive;
        rate = RequestLimiterProperties.default_rate;
        strategy = RequestLimiterProperties.default_limiterStrategy;
        timeoutInMilliseconds = RequestLimiterProperties.default_timeoutInMilliseconds;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public int getStrategy() {
        return strategy;
    }

    public void setStrategy(int strategy) {
        this.strategy = strategy;
    }

    public long getTimeoutInMilliseconds() {
        return timeoutInMilliseconds;
    }

    public void setTimeoutInMilliseconds(long timeoutInMilliseconds) {
        this.timeoutInMilliseconds = timeoutInMilliseconds;
    }

    @Override
    public String toString() {
        return "RequestLimiterBean{" + "isActive=" + isActive + ", rate=" + rate + ", strategy="
                + strategy + ", timeoutInMilliseconds=" + timeoutInMilliseconds + '}';
    }
}
