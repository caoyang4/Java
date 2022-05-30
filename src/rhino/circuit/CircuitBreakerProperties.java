package src.rhino.circuit;

import java.util.List;
import java.util.Set;

import src.rhino.RhinoProperties;
import src.rhino.circuit.forceopen.ForceOpenDegradeStrategy;
import src.rhino.circuit.recover.RecoverStrategy;
import src.rhino.circuit.trigger.TriggerStrategy;
import src.rhino.config.Configuration;

/**
 * @author zhanjun on 2017/4/21.
 */
public interface CircuitBreakerProperties extends RhinoProperties {

    boolean default_isActive = false;
    boolean default_isForceOpen = false;
    boolean default_isDegradeOnException = false;
    int default_sleepWindowInMilliseconds = 5000;
    int default_triggerStrategy = TriggerStrategy.Type.DEFAULT.getCode();
    float default_errorThresholdPercentage = 50.0f;
    int default_errorThresholdCount = 2;
    int default_requestVolumeThreshold = 20;
    int default_rollingStatsTime = 10;
    long default_timeoutInMilliseconds = 0;
    int default_recoverStrategy = RecoverStrategy.Type.SmoothRecover.getCode();
    int default_recoverTimeInSeconds = 10;
    int default_recoverDelayInSeconds = 0;
    int default_forceOpenDegradeStrategy = ForceOpenDegradeStrategy.Type.NoOp.getCode();
    int default_forceOpenPercent = 100;

    /**
     * return the properties used by test requests
     *
     * @return
     */
    CircuitBreakerProperties forkTestProperties();

    /**
     * return the component is active or not
     *
     * @return
     */
    boolean getIsActive();

    /**
     * return if circuitBreaker is force to open or not
     *
     * @return
     */
    boolean getIsForceOpen();

    /**
     * return the time to sleep when test is fail
     *
     * @return
     */
    int getSleepWindowInMilliseconds();

    /**
     * return strategy to open circuit breaker
     *
     * @return
     */
    int getTriggerStrategy();

    /**
     * return the error threshold percentage to open circuitBreaker
     *
     * @return
     */
    float getErrorThresholdPercentage();

    /**
     * return the error threshold count to open circuitBreaker
     *
     * @return
     */
    int getErrorThresholdCount();

    /**
     * return the volume threshold of request to check error threshold percentage
     *
     * @return
     */
    int getRequestVolumeThreshold();

    /**
     * return the rolling statistics data time
     *
     * @return
     */
    int getRollingStatsTime();

    /**
     * return the request time out
     *
     * @return
     */
    long getTimeoutInMilliseconds();

    /**
     * return strategy to recover request
     *
     * @return
     */
    int getRecoverStrategy();

    /**
     * return the limit time to recover request
     *
     * @return
     */
    int getRecoverTimeInSeconds();

    /**
     * @return
     */
    int getRecoverDelayInSeconds();

    /**
     * is return degrade result when exception occur
     *
     * @return
     */
    boolean getIsDegradeOnException();

    /**
     * return the degrade strategy code
     * 0：默认
     * 1：返回特定值
     * 2：抛出异常
     * 3：执行脚本
     * 4：Mock
     *
     * @return
     */
    int getDegradeStrategy();

    /**
     * 降级方法具体内容
     *
     * @return
     */
    String getDegradeStrategyValue();

    /**
     * 信号量
     *
     * @return
     */
    int getSemaphorePermits();

    /**
     * 手动降级开启降级策略
     *
     * @return
     */
    int getForceOpenDegradeStrategy();

    /**
     * 手动降级开启降级请求比例
     *
     * @return
     */
    int getForceOpenDegradePercent();

    /**
     * 分时段熔断触发条件
     *
     * @return
     */
    List<CircuitBreakerTriggerRangeData> getCircuitBreakerTriggerRangeDataList();

    /**
     * 忽略的异常集合
     *
     * @return
     */
    Set<Class<? extends Exception>> getIgnoredExceptions();

    class Factory {

        /**
         * create DefaultCircuitBreakerProperties with specified rhino key
         *
         * @param rhinoKey
         * @return
         */
        public static CircuitBreakerProperties create(String rhinoKey) {
            return new DefaultCircuitBreakerProperties(rhinoKey);
        }

        /**
         * create DefaultCircuitBreakerProperties with specified app key, rhino key and configuration
         *
         * @param appKey
         * @param rhinoKey
         * @param configuration
         * @return
         */
        public static CircuitBreakerProperties create(String appKey, String rhinoKey, Configuration configuration) {
            return new DefaultCircuitBreakerProperties(appKey, rhinoKey, DefaultCircuitBreakerProperties.Setter(), configuration);
        }
    }
}
