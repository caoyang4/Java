package src.rhino.retry;

import java.util.Set;

/**
 * Created by zhen on 2019/2/20.
 */
public class RetryPropertiesBean {

    /**
     * 开关
     */
    private boolean active = RetryProperties.ACTIVE;
    /**
     * 重试次数
     */
    private int maxAttempts = RetryProperties.MAX_ATTEMPTS;
    /**
     * 延迟时间计算类型
     */
    private int delayStrategy = RetryProperties.DELAY_STRATEGY;
    /**
     * 延迟时间，单位 millisecond
     */
    private long delay = RetryProperties.DELAY;

    private long minDelay = RetryProperties.MIN_DELAY;

    private long maxDelay = RetryProperties.MAX_DELAY;

    /**
     * 最大持续时间，单位 millisecond
     */
    private long maxDuration = RetryProperties.MAX_DURATION;

    private double multiplier = RetryProperties.MULTIPLIER;

    private Set<Class<? extends Throwable>> retryForExceptions;

    private Set<Class<? extends Throwable>> ignoredExceptions;

    public RetryPropertiesBean() {
    }

    public RetryPropertiesBean(DefaultRetryProperties.Setter setter) {
        this.active = setter.isActive();
        this.maxAttempts = setter.getMaxAttempts();
        this.delayStrategy = setter.getDelayStrategy();
        this.delay = setter.getDelay();
        this.maxDelay = setter.getMaxDelay();
        this.minDelay = setter.getMinDelay();
        this.maxDuration = setter.getMaxDuration();
        this.multiplier = setter.getMultiplier();
        this.ignoredExceptions = setter.getIgnoredExceptions();
        this.retryForExceptions = setter.getRetryForExceptions();
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public int getDelayStrategy() {
        return delayStrategy;
    }

    public void setDelayStrategy(int delayStrategy) {
        this.delayStrategy = delayStrategy;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public long getMinDelay() {
        return minDelay;
    }

    public void setMinDelay(long minDelay) {
        this.minDelay = minDelay;
    }

    public long getMaxDelay() {
        return maxDelay;
    }

    public void setMaxDelay(long maxDelay) {
        this.maxDelay = maxDelay;
    }

    public long getMaxDuration() {
        return maxDuration;
    }

    public void setMaxDuration(long maxDuration) {
        this.maxDuration = maxDuration;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    public Set<Class<? extends Throwable>> getRetryForExceptions() {
        return retryForExceptions;
    }

    public void setRetryForExceptions(Set<Class<? extends Throwable>> retryForExceptions) {
        this.retryForExceptions = retryForExceptions;
    }

    public Set<Class<? extends Throwable>> getIgnoredExceptions() {
        return ignoredExceptions;
    }

    public void setIgnoredExceptions(Set<Class<? extends Throwable>> ignoredExceptions) {
        this.ignoredExceptions = ignoredExceptions;
    }


}
