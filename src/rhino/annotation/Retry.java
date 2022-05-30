package src.rhino.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import src.rhino.retry.RetryProperties;
import src.rhino.retry.listener.RetryListener;

/**
 * Created by zhen on 2019/3/12.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD })
public @interface Retry {

    /**
     * @return rhino key
     */
    String rhinoKey();

    /**
     * 恢复方法
     * @return
     */
    String recoverMethod() default "";

    /**
     * listeners
     * @return
     */
    Class<? extends RetryListener>[] retryListeners() default {};

    /**
     * 是否启用，默认开启
     * @return
     */
    boolean isActive() default RetryProperties.ACTIVE;

    /**
     * 重试次数，默认重试3次
     * @return
     */
    int maxAttempts() default RetryProperties.MAX_ATTEMPTS;

    /**
     * 重试退避策略
     * 0：立即重试
     * 1：sleep 固定时间
     * 2：sleep 固定范围内的随机间隔
     * 3：sleep 指数级别增长时间
     * 4：sleep 指数级别增长随机时间
     * @return
     */
    int delayStrategy() default RetryProperties.DELAY_STRATEGY;

    /**
     * sleep 时间，默认500，单位毫秒
     * @return
     */
    long delay() default RetryProperties.DELAY;

    /**
     * sleep 最短时间，默认0，单位毫秒
     * @return
     */
    long minDelay() default RetryProperties.MIN_DELAY;

    /**
     * sleep 最长时间，默认10000，单位毫秒
     * @return
     */
    long maxDelay() default RetryProperties.MAX_DELAY;

    /**
     * 重试最大持续时长，默认30秒，单位毫秒。
     * 如超过该值，则不满足重试条件
     * @return
     */
    long maxDuration() default RetryProperties.MAX_DURATION;

    /**
     * 指数级别退避策略的乘数，默认2.0。
     * @return
     */
    double multiplier() default RetryProperties.MULTIPLIER;

    /**
     * 重试可忽略异常。默认为空
     * 如捕获到列表中的异常，则重试结束
     * @return
     */
    Class<? extends Throwable>[] ignoreExceptions() default {};

    /**
     * 重试异常。默认为空，即所有异常都需重试。
     * 只有捕获到列表中的异常，才会重试
     * @return
     */
    Class<? extends Throwable>[] retryOnExceptions() default {};
}
