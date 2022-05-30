package src.rhino.circuit;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.*;

import com.mysql.cj.util.StringUtils;

import src.rhino.RhinoConfigProperties;
import src.rhino.RhinoType;
import src.rhino.annotation.Degrade;
import src.rhino.config.ConfigChangedListener;
import src.rhino.config.Configuration;
import src.rhino.config.PropertyChangedListener;
import src.rhino.util.AppUtils;
import src.rhino.util.SerializerUtils;

/**
 * @author zhanjun on 2017/4/21.
 */
public class DefaultCircuitBreakerProperties extends RhinoConfigProperties implements CircuitBreakerProperties {

    private static final String TRANSACTION_TYPE = "Rhino.CircuitBreaker.PropertyChange";

    private boolean isTest = false;

    private volatile CircuitBreakerPropertyData circuitBreakerPropertyData;

    private DefaultCircuitBreakerProperties testProperties;

    public DefaultCircuitBreakerProperties(String rhinoKey) {
        this(rhinoKey, false);
    }

    public DefaultCircuitBreakerProperties(String rhinoKey, boolean active) {
        this(rhinoKey, new Setter().withActive(active));
    }

    public DefaultCircuitBreakerProperties(String rhinoKey, Setter setter) {
        this(AppUtils.getAppName(), rhinoKey, setter, null);
    }

    public DefaultCircuitBreakerProperties(String appKey, String rhinoKey, Setter setter, Configuration config) {
        super(appKey, rhinoKey, RhinoType.CircuitBreaker, config);
        String value = getStringValue(configKeySuffix, "");
        CircuitBreakerPropertyData circuitBreakerPropertyData = parseCircuitBreakerPropertyData(value);
        if (circuitBreakerPropertyData == null) {
            if (setter == null) {
                setter = Setter();
            }
            circuitBreakerPropertyData = new CircuitBreakerPropertyData(setter);
        }
        this.circuitBreakerPropertyData = circuitBreakerPropertyData;
    }

    public DefaultCircuitBreakerProperties(String rhinoKey, CircuitBreakerPropertyData data, Configuration config){
        super(AppUtils.getAppName(), rhinoKey, RhinoType.CircuitBreaker, config);
        this.isTest = true;
        this.circuitBreakerPropertyData = data;
    }

    @Override
    public DefaultCircuitBreakerProperties forkTestProperties(){
        if(isTest){
            throw new IllegalArgumentException(rhinoKey + ": Original circuit properties is test, can't be forked!");
        }

        if(testProperties == null){
            testProperties = new DefaultCircuitBreakerProperties(rhinoKey, circuitBreakerPropertyData, null);
        }
        return testProperties;
    }

    @Override
    public void addConfigChangedListener(ConfigChangedListener listener) {
        addPropertiesChangedListener(configKeySuffix, new ConfigChangedListener() {
            private Object lock = new Object();

            @Override
            public void invoke(String key, String oldValue, String newValue) {
                synchronized (lock) {
                    if (newValue == null || Objects.equals(oldValue, newValue)) {
                        return;
                    }
                    CircuitBreakerPropertyData newProperties = parseCircuitBreakerPropertyData(newValue);
                    if (newProperties == null) {
                        return;
                    }
                    CircuitBreakerPropertyData oldProperties = circuitBreakerPropertyData;
                    circuitBreakerPropertyData = newProperties;
                    if(testProperties != null){
                        testProperties.circuitBreakerPropertyData = newProperties;
                    }
                    for (PropertyChangedListener propertyChangedListener : propertyChangedListeners) {
                        propertyChangedListener.trigger(oldProperties, newProperties);
                    }
                }
            }
        });
    }

    /**
     * parse circuit breaker property data
     *
     * @param value
     * @return
     */
    public CircuitBreakerPropertyData parseCircuitBreakerPropertyData(String value) {
        if (StringUtils.isNullOrEmpty(value)) {
            try {
                return SerializerUtils.read(URLDecoder.decode(value, "UTF-8"), CircuitBreakerPropertyData.class);
            } catch (IOException e) {
                logger.warn("fail to parse from configManager, value: " + value, e);
            }
        }
        return null;
    }

    public static Setter Setter() {
        return new Setter();
    }

    //如果当前是压测熔断器，且配置中启用了压测独立配置，就返回压测配置信息
    private boolean checkTestConfig(){
        return isTest && circuitBreakerPropertyData.isTestConfiged();
    }

    @Override
    public boolean getIsActive() {
        return checkTestConfig() ? circuitBreakerPropertyData.isTestActive() : circuitBreakerPropertyData.getActive();
    }

    @Override
    public boolean getIsForceOpen() {
        return checkTestConfig() ? circuitBreakerPropertyData.isTestForceOpen() : circuitBreakerPropertyData.getForceOpen();
    }

    @Override
    public int getSleepWindowInMilliseconds() {
        return checkTestConfig() ? circuitBreakerPropertyData.getTestSleepWindowInMilliseconds() : circuitBreakerPropertyData.getSleepWindowInMilliseconds();
    }

    @Override
    public float getErrorThresholdPercentage() {
        return checkTestConfig() ? circuitBreakerPropertyData.getTestErrorThresholdPercentage() : circuitBreakerPropertyData.getErrorThresholdPercentage();
    }

    @Override
    public int getErrorThresholdCount() {
        return checkTestConfig() ? circuitBreakerPropertyData.getTestErrorThresholdCount() : circuitBreakerPropertyData.getErrorThresholdCount();
    }

    @Override
    public int getRequestVolumeThreshold() {
        return checkTestConfig() ? circuitBreakerPropertyData.getTestRequestVolumeThreshold() : circuitBreakerPropertyData.getRequestVolumeThreshold();
    }

    @Override
    public int getRollingStatsTime() {
        return checkTestConfig() ? circuitBreakerPropertyData.getTestRollingStatsTime() : circuitBreakerPropertyData.getRollingStatsTime();
    }

    @Override
    public long getTimeoutInMilliseconds() {
        return checkTestConfig() ? circuitBreakerPropertyData.getTestTimeoutInMilliseconds() : circuitBreakerPropertyData.getTimeoutInMilliseconds();
    }

    @Override
    public int getTriggerStrategy() {
        return checkTestConfig() ? circuitBreakerPropertyData.getTestTriggerStrategy() : circuitBreakerPropertyData.getTriggerStrategy();
    }

    @Override
    public int getRecoverStrategy() {
        return checkTestConfig() ? circuitBreakerPropertyData.getTestRecoverStrategy() : circuitBreakerPropertyData.getRecoverStrategy();
    }

    @Override
    public int getRecoverTimeInSeconds() {
        return checkTestConfig() ? circuitBreakerPropertyData.getTestRecoverTimeInSeconds() : circuitBreakerPropertyData.getRecoverTimeInSeconds();
    }

    @Override
    public int getRecoverDelayInSeconds() {
        return checkTestConfig() ? circuitBreakerPropertyData.getTestRecoverDelayInSeconds() : circuitBreakerPropertyData.getRecoverDelayInSeconds();
    }

    @Override
    public boolean getIsDegradeOnException() {
        return checkTestConfig() ? circuitBreakerPropertyData.isTestDegradeOnException() : circuitBreakerPropertyData.getDegradeOnException();
    }

    @Override
    public int getDegradeStrategy() {
        return checkTestConfig() ? circuitBreakerPropertyData.getTestDegradeStrategy() : circuitBreakerPropertyData.getDegradeStrategy();
    }

    @Override
    public String getDegradeStrategyValue() {
        return checkTestConfig() ? circuitBreakerPropertyData.getTestDegradeStrategyValue() : circuitBreakerPropertyData.getDegradeStrategyValue();
    }

    @Override
    public int getSemaphorePermits() {
        return checkTestConfig() ? circuitBreakerPropertyData.getTestSemaphorePermits() : circuitBreakerPropertyData.getSemaphorePermits();
    }

    @Override
    public int getForceOpenDegradeStrategy() {
        return checkTestConfig() ? circuitBreakerPropertyData.getTestForceOpenDegradeStrategy() : circuitBreakerPropertyData.getForceOpenDegradeStrategy();
    }

    @Override
    public int getForceOpenDegradePercent() {
        return checkTestConfig() ? circuitBreakerPropertyData.getTestForceOpenDegradePercent() : circuitBreakerPropertyData.getForceOpenDegradePercent();
    }

    @Override
    public List<CircuitBreakerTriggerRangeData> getCircuitBreakerTriggerRangeDataList() {
        return checkTestConfig() ? circuitBreakerPropertyData.getTestCircuitBreakerTriggerRangeDataList() : circuitBreakerPropertyData.getCircuitBreakerTriggerRangeDataList();
    }

    @Override
    public Set<Class<? extends Exception>> getIgnoredExceptions() {
        return checkTestConfig() ? circuitBreakerPropertyData.getTestIgnoredExceptions() : circuitBreakerPropertyData.getIgnoredExceptions();
    }

    @Override
    public String toJson() {
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("{\"");
            builder.append(configKeySuffix);
            builder.append("\":");
            builder.append(SerializerUtils.write(circuitBreakerPropertyData));
            builder.append("}");
            return builder.toString();
        } catch (IOException e) {
            logger.warn("DefaultCircuitBreakerProperties toJson error" + e.getMessage());
        }
        return "";
    }

    public final static class Setter {
        private boolean active = default_isActive;
        private boolean forceOpen = default_isForceOpen;
        private boolean degradeOnException = default_isDegradeOnException;
        private int sleepWindowInMilliseconds = default_sleepWindowInMilliseconds;
        private int triggerStrategy = default_triggerStrategy;
        private float errorThresholdPercentage = default_errorThresholdPercentage;
        private int errorThresholdCount = default_errorThresholdCount;
        private int requestVolumeThreshold = default_requestVolumeThreshold;
        private int rollingStatsTime = default_rollingStatsTime;
        private long timeoutInMilliseconds = default_timeoutInMilliseconds;
        private int recoverStrategy = default_recoverStrategy;
        private int recoverTimeInSeconds = default_recoverTimeInSeconds;
        private int recoverDelayInSeconds = default_recoverDelayInSeconds;
        private int degradeStrategy = 0;
        private String degradeStrategyValue;
        private int semaphorePermits = 0;
        private int forceOpenStrategy = default_forceOpenDegradeStrategy;
        private int forceOpenPercent = default_forceOpenPercent;
        private Set<Class<? extends Exception>> ignoredExceptions;

        public Setter() {
        }

        public Setter(Degrade degrade) {
            this.active = degrade.isActive();
            this.forceOpen = degrade.isForceOpen();
            this.degradeOnException = degrade.isDegradeOnException();
            this.sleepWindowInMilliseconds = degrade.sleepWindowInMilliseconds();
            this.errorThresholdPercentage = degrade.errorThresholdPercentage();
            this.errorThresholdCount = degrade.errorThresholdCount();
            this.requestVolumeThreshold = degrade.requestVolumeThreshold();
            this.rollingStatsTime = degrade.rollingStatsTime();
            this.timeoutInMilliseconds = degrade.timeoutInMilliseconds();
            this.triggerStrategy = degrade.triggerStrategy().getCode();
            this.recoverStrategy = degrade.recoverStrategy().getCode();
            this.recoverTimeInSeconds = degrade.recoverTimeInSeconds();
            this.recoverDelayInSeconds = degrade.recoverDelayInSeconds();
            this.semaphorePermits = degrade.semaphorePermits();
            Class<? extends Exception>[] ignoreExceptions = degrade.ignoreExceptions();
            if (ignoreExceptions != null && ignoreExceptions.length > 0) {
                this.ignoredExceptions = new HashSet<>(Arrays.asList(ignoreExceptions));
            }
        }

        public boolean isActive() {
            return active;
        }

        public boolean isForceOpen() {
            return forceOpen;
        }

        public boolean isDegradeOnException() {
            return degradeOnException;
        }

        public int getSleepWindowInMilliseconds() {
            return sleepWindowInMilliseconds;
        }

        public int getTriggerStrategy() {
            return triggerStrategy;
        }

        public float getErrorThresholdPercentage() {
            return errorThresholdPercentage;
        }

        public int getErrorThresholdCount() {
            return errorThresholdCount;
        }

        public int getRequestVolumeThreshold() {
            return requestVolumeThreshold;
        }

        public int getRollingStatsTime() {
            return rollingStatsTime;
        }

        public long getTimeoutInMilliseconds() {
            return timeoutInMilliseconds;
        }

        public int getRecoverStrategy() {
            return recoverStrategy;
        }

        public int getRecoverTimeInSeconds() {
            return recoverTimeInSeconds;
        }

        public int getRecoverDelayInSeconds() {
            return recoverDelayInSeconds;
        }

        public int getDegradeStrategy() {
            return degradeStrategy;
        }

        public String getDegradeStrategyValue() {
            return degradeStrategyValue;
        }

        public int getSemaphorePermits() {
            return semaphorePermits;
        }

        public Set<Class<? extends Exception>> getIgnoredExceptions() {
            return ignoredExceptions;
        }


        public Setter withActive(boolean active) {
            this.active = active;
            return this;
        }

        public Setter withForceOpen(boolean forceOpen) {
            this.forceOpen = forceOpen;
            return this;
        }

        public Setter withDegradeOnException(boolean degradeOnException) {
            this.degradeOnException = degradeOnException;
            return this;
        }

        public Setter withSleepWindowInMilliseconds(int sleepWindowInMilliseconds) {
            this.sleepWindowInMilliseconds = sleepWindowInMilliseconds;
            return this;
        }

        public Setter withTriggerStrategy(int triggerStrategy) {
            this.triggerStrategy = triggerStrategy;
            return this;
        }

        public Setter withErrorThresholdPercentage(float errorThresholdPercentage) {
            this.errorThresholdPercentage = errorThresholdPercentage;
            return this;
        }

        public Setter withErrorThresholdCount(int errorThresholdCount) {
            this.errorThresholdCount = errorThresholdCount;
            return this;
        }

        public Setter withRequestVolumeThreshold(int requestVolumeThreshold) {
            this.requestVolumeThreshold = requestVolumeThreshold;
            return this;
        }

        public Setter withRollingStatsTime(int rollingStatsTime) {
            this.rollingStatsTime = rollingStatsTime;
            return this;
        }

        public Setter withTimeoutInMilliseconds(long timeoutInMilliseconds) {
            this.timeoutInMilliseconds = timeoutInMilliseconds;
            return this;
        }

        public Setter withRecoverStrategy(int recoverStrategy) {
            this.recoverStrategy = recoverStrategy;
            return this;
        }

        public Setter withRecoverTimeInSeconds(int recoverTimeInSeconds) {
            this.recoverTimeInSeconds = recoverTimeInSeconds;
            return this;
        }

        public Setter withRecoverDelayInSeconds(int recoverDelayInSeconds) {
            this.recoverDelayInSeconds = recoverDelayInSeconds;
            return this;
        }

        public Setter withDegradeStrategy(int degradeStrategy) {
            this.degradeStrategy = degradeStrategy;
            return this;
        }

        public Setter withDegradeStrategyValue(String degradeStrategyValue) {
            this.degradeStrategyValue = degradeStrategyValue;
            return this;
        }

        public Setter withSemaphorePermits(int semaphorePermits) {
            this.semaphorePermits = semaphorePermits;
            return this;
        }

        public Setter withIgnoredExceptions(Set<Class<? extends Exception>> ignoredExceptions) {
            this.ignoredExceptions = ignoredExceptions;
            return this;
        }

        public int getForceOpenStrategy() {
            return forceOpenStrategy;
        }

        public void setForceOpenStrategy(int forceOpenStrategy) {
            this.forceOpenStrategy = forceOpenStrategy;
        }

        public int getForceOpenPercent() {
            return forceOpenPercent;
        }

        public void setForceOpenPercent(int forceOpenPercent) {
            this.forceOpenPercent = forceOpenPercent;
        }
    }
}
