package src.rhino.retry;

import src.rhino.RhinoProperties;

/**
 * Created by zhen on 2019/2/20.
 */
public interface RetryProperties extends RhinoProperties {

    boolean ACTIVE = true;
    int MAX_ATTEMPTS = 3;
    int DELAY_STRATEGY = 1;
    long DELAY = 500;
    long MIN_DELAY = 0;
    long MAX_DELAY = 5000;
    long MAX_DURATION = 30000;
    double MULTIPLIER = 2.0;

    boolean isActive();

    /**
     * 是否可以重试
     * @param retryContext
     * @return
     */
    boolean canRetry(RetryContext retryContext);

    /**
     * 获取延迟时间的计算策略
     *
     * @return
     */
    int getDelayStrategy();

    long getDelay();

    long getMinDelay();

    long getMaxDelay();

    int getMaxAttempts();

    long getMaxDuration();

    double getMultiplier();


}
