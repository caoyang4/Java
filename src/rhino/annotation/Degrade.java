package src.rhino.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import src.rhino.circuit.CircuitBreakerListener;
import src.rhino.circuit.CircuitBreakerProperties;
import src.rhino.circuit.listener.CircuitBreakerNoOpListener;
import src.rhino.circuit.recover.RecoverStrategy;
import src.rhino.circuit.trigger.TriggerStrategy;

/**
 * Created by zhanjun on 2017/4/21.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Degrade {

    /**
     * @return rhino key
     */
    String rhinoKey();

    /**
     * @return fall back method name
     */
    String fallBackMethod();

    /**
     * @return CircuitBreakerListener triggered when circuit breaker open or close
     */
    Class<? extends CircuitBreakerListener> circuitBreakerListener() default CircuitBreakerNoOpListener.class;

    /**
     *
     * @return is this degrade active
     */
    boolean isActive() default true;

    /**
     *
     * @return is force to degrade
     */
    boolean isForceOpen() default CircuitBreakerProperties.default_isForceOpen;

    /**
     *
     * @return is degrade when exception occur
     */
    boolean isDegradeOnException() default CircuitBreakerProperties.default_isDegradeOnException;

    /**
     * default 1.0f
     * @return error threshold percentage to degrade
     */
    float errorThresholdPercentage() default CircuitBreakerProperties.default_errorThresholdPercentage;

    /**
     * default 10
     * @return error threshold percentage to degrade
     */
    int errorThresholdCount() default CircuitBreakerProperties.default_errorThresholdCount;

    /**
     * default 5000
     * @return sleep window in milliseconds
     */
    int sleepWindowInMilliseconds() default CircuitBreakerProperties.default_sleepWindowInMilliseconds;

    /**
     * default 20
     * @return request count
     */
    int requestVolumeThreshold() default CircuitBreakerProperties.default_requestVolumeThreshold;

    /**
     * default 10
     * @return
     */
    int rollingStatsTime() default CircuitBreakerProperties.default_rollingStatsTime;

    /**
     * default 0
     * @return timeout in milliseconds
     */
    long timeoutInMilliseconds() default CircuitBreakerProperties.default_timeoutInMilliseconds;

    /**
     *
     * @return the strategy to recover request
     */
    RecoverStrategy.Type recoverStrategy() default RecoverStrategy.Type.SmoothRecover;

    /**
     * only use for the strategy is TimeRecover
     * @return
     */
    int recoverTimeInSeconds() default CircuitBreakerProperties.default_recoverTimeInSeconds;

    /**
     * default 0
     * @return
     */
    int recoverDelayInSeconds() default CircuitBreakerProperties.default_recoverDelayInSeconds;

    /**
     *
     * @return the strategy to recover request
     */
    TriggerStrategy.Type triggerStrategy() default TriggerStrategy.Type.DEFAULT;

    /**
     * ignorable exception list, 忽略方法中抛出的特定异常，不会对这些异常进行统计，直接抛给业务进行处理
     * @returns
     */
    Class<? extends Exception>[] ignoreExceptions() default {};

    /**
     * 信号量限制，默认为0，不启用
     * @return
     */
    int semaphorePermits() default 0;
}