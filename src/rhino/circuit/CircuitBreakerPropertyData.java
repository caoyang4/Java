package src.rhino.circuit;

import java.util.List;
import java.util.Set;

/**
 * @author zhen on 2017/10/30.
 */
public class CircuitBreakerPropertyData {
    private boolean active;
    private boolean forceOpen;
    private boolean degradeOnException;
    private int sleepWindowInMilliseconds;
    private int triggerStrategy;
    private float errorThresholdPercentage;
    private int errorThresholdCount;
    private int requestVolumeThreshold;
    private int rollingStatsTime;
    private long timeoutInMilliseconds;
    private int recoverStrategy;
    private int recoverTimeInSeconds;
    private int recoverDelayInSeconds;
    private int degradeStrategy;
    private String degradeStrategyValue;
    private int semaphorePermits;
    private int forceOpenDegradeStrategy = 0;
    private int forceOpenDegradePercent = 100;
    private List<CircuitBreakerTriggerRangeData> circuitBreakerTriggerRangeDataList;
    private Set<Class<? extends Exception>> ignoredExceptions;

    //压测配置
    private boolean testConfiged = false;
    private boolean testActive;
    private boolean testForceOpen;
    private boolean testDegradeOnException;
    private int testSleepWindowInMilliseconds;
    private int testTriggerStrategy;
    private float testErrorThresholdPercentage;
    private int testErrorThresholdCount;
    private int testRequestVolumeThreshold;
    private int testRollingStatsTime;
    private long testTimeoutInMilliseconds;
    private int testRecoverStrategy;
    private int testRecoverTimeInSeconds;
    private int testRecoverDelayInSeconds;
    private int testDegradeStrategy;
    private String testDegradeStrategyValue;
    private int testSemaphorePermits;
    private int testForceOpenDegradeStrategy = 0;
    private int testForceOpenDegradePercent = 100;
    private List<CircuitBreakerTriggerRangeData> testCircuitBreakerTriggerRangeDataList;
    private Set<Class<? extends Exception>> testIgnoredExceptions;

    public CircuitBreakerPropertyData() {
    }

    public CircuitBreakerPropertyData(DefaultCircuitBreakerProperties.Setter setter) {
        this.active = setter.isActive();
        this.forceOpen = setter.isForceOpen();
        this.degradeOnException = setter.isDegradeOnException();
        this.degradeStrategy = setter.getDegradeStrategy();
        this.degradeStrategyValue = setter.getDegradeStrategyValue();
        this.sleepWindowInMilliseconds = setter.getSleepWindowInMilliseconds();
        this.errorThresholdPercentage = setter.getErrorThresholdPercentage();
        this.errorThresholdCount = setter.getErrorThresholdCount();
        this.requestVolumeThreshold = setter.getRequestVolumeThreshold();
        this.rollingStatsTime = setter.getRollingStatsTime();
        this.timeoutInMilliseconds = setter.getTimeoutInMilliseconds();
        this.triggerStrategy = setter.getTriggerStrategy();
        this.recoverStrategy = setter.getRecoverStrategy();
        this.recoverTimeInSeconds = setter.getRecoverTimeInSeconds();
        this.recoverDelayInSeconds = setter.getRecoverDelayInSeconds();
        this.semaphorePermits = setter.getSemaphorePermits();
        this.ignoredExceptions = setter.getIgnoredExceptions();
        this.forceOpenDegradeStrategy = setter.getForceOpenStrategy();
        this.forceOpenDegradePercent = setter.getForceOpenPercent();
    }

    public boolean getActive() {
        return active;
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

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean getForceOpen() {
        return forceOpen;
    }

    public void setForceOpen(boolean forceOpen) {
        this.forceOpen = forceOpen;
    }

    public boolean getDegradeOnException() {
        return degradeOnException;
    }

    public void setDegradeOnException(boolean degradeOnException) {
        this.degradeOnException = degradeOnException;
    }

    public int getSleepWindowInMilliseconds() {
        return sleepWindowInMilliseconds;
    }

    public void setSleepWindowInMilliseconds(int sleepWindowInMilliseconds) {
        this.sleepWindowInMilliseconds = sleepWindowInMilliseconds;
    }

    public int getTriggerStrategy() {
        return triggerStrategy;
    }

    public void setTriggerStrategy(int triggerStrategy) {
        this.triggerStrategy = triggerStrategy;
    }

    public float getErrorThresholdPercentage() {
        return errorThresholdPercentage;
    }

    public void setErrorThresholdPercentage(float errorThresholdPercentage) {
        this.errorThresholdPercentage = errorThresholdPercentage;
    }

    public int getErrorThresholdCount() {
        return errorThresholdCount;
    }

    public void setErrorThresholdCount(int errorThresholdCount) {
        this.errorThresholdCount = errorThresholdCount;
    }

    public int getRequestVolumeThreshold() {
        return requestVolumeThreshold;
    }

    public void setRequestVolumeThreshold(int requestVolumeThreshold) {
        this.requestVolumeThreshold = requestVolumeThreshold;
    }

    public int getRollingStatsTime() {
        return rollingStatsTime;
    }

    public void setRollingStatsTime(int rollingStatsTime) {
        this.rollingStatsTime = rollingStatsTime;
    }

    public long getTimeoutInMilliseconds() {
        return timeoutInMilliseconds;
    }

    public void setTimeoutInMilliseconds(long timeoutInMilliseconds) {
        this.timeoutInMilliseconds = timeoutInMilliseconds;
    }

    public int getRecoverStrategy() {
        return recoverStrategy;
    }

    public void setRecoverStrategy(int recoverStrategy) {
        this.recoverStrategy = recoverStrategy;
    }

    public int getRecoverTimeInSeconds() {
        return recoverTimeInSeconds;
    }

    public void setRecoverTimeInSeconds(int recoverTimeInSeconds) {
        this.recoverTimeInSeconds = recoverTimeInSeconds;
    }

    public int getRecoverDelayInSeconds() {
        return recoverDelayInSeconds;
    }

    public void setRecoverDelayInSeconds(int recoverDelayInSeconds) {
        this.recoverDelayInSeconds = recoverDelayInSeconds;
    }

    public int getDegradeStrategy() {
        return degradeStrategy;
    }

    public void setDegradeStrategy(int degradeStrategy) {
        this.degradeStrategy = degradeStrategy;
    }

    public String getDegradeStrategyValue() {
        return degradeStrategyValue;
    }

    public void setDegradeStrategyValue(String degradeStrategyValue) {
        this.degradeStrategyValue = degradeStrategyValue;
    }

    public int getSemaphorePermits() {
        return semaphorePermits;
    }

    public void setSemaphorePermits(int semaphorePermits) {
        this.semaphorePermits = semaphorePermits;
    }

    public List<CircuitBreakerTriggerRangeData> getCircuitBreakerTriggerRangeDataList() {
        return circuitBreakerTriggerRangeDataList;
    }

    public void setCircuitBreakerTriggerRangeDataList(List<CircuitBreakerTriggerRangeData> circuitBreakerTriggerRangeDataList) {
        this.circuitBreakerTriggerRangeDataList = circuitBreakerTriggerRangeDataList;
    }

    public Set<Class<? extends Exception>> getIgnoredExceptions() {
        return ignoredExceptions;
    }

    public void setIgnoredExceptions(Set<Class<? extends Exception>> ignoredExceptions) {
        this.ignoredExceptions = ignoredExceptions;
    }

    public int getForceOpenDegradeStrategy() {
        return forceOpenDegradeStrategy;
    }

    public void setForceOpenDegradeStrategy(int forceOpenDegradeStrategy) {
        this.forceOpenDegradeStrategy = forceOpenDegradeStrategy;
    }

    public int getForceOpenDegradePercent() {
        return forceOpenDegradePercent;
    }

    public void setForceOpenDegradePercent(int forceOpenDegradePercent) {
        this.forceOpenDegradePercent = forceOpenDegradePercent;
    }

    public boolean isTestConfiged() {
        return testConfiged;
    }

    public void setTestConfiged(boolean testConfiged) {
        this.testConfiged = testConfiged;
    }

    public boolean isTestActive() {
        return testActive;
    }

    public void setTestActive(boolean testActive) {
        this.testActive = testActive;
    }

    public boolean isTestForceOpen() {
        return testForceOpen;
    }

    public void setTestForceOpen(boolean testForceOpen) {
        this.testForceOpen = testForceOpen;
    }

    public boolean isTestDegradeOnException() {
        return testDegradeOnException;
    }

    public void setTestDegradeOnException(boolean testDegradeOnException) {
        this.testDegradeOnException = testDegradeOnException;
    }

    public int getTestSleepWindowInMilliseconds() {
        return testSleepWindowInMilliseconds;
    }

    public void setTestSleepWindowInMilliseconds(int testSleepWindowInMilliseconds) {
        this.testSleepWindowInMilliseconds = testSleepWindowInMilliseconds;
    }

    public int getTestTriggerStrategy() {
        return testTriggerStrategy;
    }

    public void setTestTriggerStrategy(int testTriggerStrategy) {
        this.testTriggerStrategy = testTriggerStrategy;
    }

    public float getTestErrorThresholdPercentage() {
        return testErrorThresholdPercentage;
    }

    public void setTestErrorThresholdPercentage(float testErrorThresholdPercentage) {
        this.testErrorThresholdPercentage = testErrorThresholdPercentage;
    }

    public int getTestRecoverTimeInSeconds() {
        return testRecoverTimeInSeconds;
    }

    public void setTestRecoverTimeInSeconds(int testRecoverTimeInSeconds) {
        this.testRecoverTimeInSeconds = testRecoverTimeInSeconds;
    }

    public int getTestErrorThresholdCount() {
        return testErrorThresholdCount;
    }

    public void setTestErrorThresholdCount(int testErrorThresholdCount) {
        this.testErrorThresholdCount = testErrorThresholdCount;
    }

    public int getTestRequestVolumeThreshold() {
        return testRequestVolumeThreshold;
    }

    public void setTestRequestVolumeThreshold(int testRequestVolumeThreshold) {
        this.testRequestVolumeThreshold = testRequestVolumeThreshold;
    }

    public int getTestRollingStatsTime() {
        return testRollingStatsTime;
    }

    public void setTestRollingStatsTime(int testRollingStatsTime) {
        this.testRollingStatsTime = testRollingStatsTime;
    }

    public long getTestTimeoutInMilliseconds() {
        return testTimeoutInMilliseconds;
    }

    public void setTestTimeoutInMilliseconds(long testTimeoutInMilliseconds) {
        this.testTimeoutInMilliseconds = testTimeoutInMilliseconds;
    }

    public int getTestRecoverStrategy() {
        return testRecoverStrategy;
    }

    public void setTestRecoverStrategy(int testRecoverStrategy) {
        this.testRecoverStrategy = testRecoverStrategy;
    }

    public int getTestRecoverDelayInSeconds() {
        return testRecoverDelayInSeconds;
    }

    public void setTestRecoverDelayInSeconds(int testRecoverDelayInSeconds) {
        this.testRecoverDelayInSeconds = testRecoverDelayInSeconds;
    }

    public int getTestDegradeStrategy() {
        return testDegradeStrategy;
    }

    public void setTestDegradeStrategy(int testDegradeStrategy) {
        this.testDegradeStrategy = testDegradeStrategy;
    }

    public String getTestDegradeStrategyValue() {
        return testDegradeStrategyValue;
    }

    public void setTestDegradeStrategyValue(String testDegradeStrategyValue) {
        this.testDegradeStrategyValue = testDegradeStrategyValue;
    }

    public int getTestSemaphorePermits() {
        return testSemaphorePermits;
    }

    public void setTestSemaphorePermits(int testSemaphorePermits) {
        this.testSemaphorePermits = testSemaphorePermits;
    }

    public int getTestForceOpenDegradeStrategy() {
        return testForceOpenDegradeStrategy;
    }

    public void setTestForceOpenDegradeStrategy(int testForceOpenDegradeStrategy) {
        this.testForceOpenDegradeStrategy = testForceOpenDegradeStrategy;
    }

    public int getTestForceOpenDegradePercent() {
        return testForceOpenDegradePercent;
    }

    public void setTestForceOpenDegradePercent(int testForceOpenDegradePercent) {
        this.testForceOpenDegradePercent = testForceOpenDegradePercent;
    }

    public List<CircuitBreakerTriggerRangeData> getTestCircuitBreakerTriggerRangeDataList() {
        return testCircuitBreakerTriggerRangeDataList;
    }

    public void setTestCircuitBreakerTriggerRangeDataList(List<CircuitBreakerTriggerRangeData> testCircuitBreakerTriggerRangeDataList) {
        this.testCircuitBreakerTriggerRangeDataList = testCircuitBreakerTriggerRangeDataList;
    }

    public Set<Class<? extends Exception>> getTestIgnoredExceptions() {
        return testIgnoredExceptions;
    }

    public void setTestIgnoredExceptions(Set<Class<? extends Exception>> testIgnoredExceptions) {
        this.testIgnoredExceptions = testIgnoredExceptions;
    }
}