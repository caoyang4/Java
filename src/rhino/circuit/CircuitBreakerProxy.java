package src.rhino.circuit;

import src.rhino.util.MtraceUtils;

/**
 * Created by zmz on 2018/11/6.
 * 区分压测流量和正常流量
 */
public class CircuitBreakerProxy implements CircuitBreaker {
    private String key;
    private CircuitBreakerProperties circuitBreakerProperties;
    private CircuitBreaker normalCircuitBreaker;
    private volatile CircuitBreaker testCircuitBreaker;
    private CircuitBreakerListener[] circuitBreakerListeners;

    public CircuitBreakerProxy(String key, CircuitBreakerProperties circuitBreakerProperties) {
        this.key = key;
        this.circuitBreakerProperties = circuitBreakerProperties;
        this.normalCircuitBreaker = new DefaultCircuitBreaker(key, circuitBreakerProperties);
    }

    /**
     * 根据Mtrace压测标识获取熔断器
     * @return
     */
    private CircuitBreaker getCircuitBreaker() {
        return MtraceUtils.isTest() ? getTestCircuitBreaker() : getNormalCircuitBreaker();
    }

    /**
     * 根据当前链路压测标识获取熔断器
     * @param circuitBreakerContext
     * @return
     */
    private CircuitBreaker getCircuitBreaker(CircuitBreakerContext circuitBreakerContext){
        if(circuitBreakerContext == null){
            return getCircuitBreaker();
        }

        return circuitBreakerContext.isTestRequest() ? getTestCircuitBreaker() : getNormalCircuitBreaker();
    }

    private CircuitBreaker getNormalCircuitBreaker(){
        return normalCircuitBreaker;
    }

    private CircuitBreaker getTestCircuitBreaker(){
        if (testCircuitBreaker == null) {
            synchronized (this) {
                if (testCircuitBreaker == null) {
                    CircuitBreakerProperties testProps = circuitBreakerProperties.forkTestProperties();
                    testCircuitBreaker = new DefaultCircuitBreaker(key + MtraceUtils.TEST_FLAG, testProps, true);
                    if (circuitBreakerListeners != null) {
                        testCircuitBreaker.setCircuitBreakerListener(circuitBreakerListeners);
                    }
                }
            }
        }
        return testCircuitBreaker;
    }

    @Override
    public boolean isEnable() {
        return getCircuitBreaker().isEnable();
    }

    @Override
    public Status getStatus() {
        return getCircuitBreaker().getStatus();
    }

    @Override
    public boolean allowRequest() {
        return getCircuitBreaker().allowRequest();
    }

    @Override
    public boolean allowRequest(CircuitBreakerContext circuitBreakerContext) {
        return getCircuitBreaker(circuitBreakerContext).allowRequest(circuitBreakerContext);
    }

    @Override
    public void setSuccess() {
        getCircuitBreaker().setSuccess();
    }

    @Override
    public void setSuccess(CircuitBreakerContext circuitBreakerContext) {
        getCircuitBreaker(circuitBreakerContext).setSuccess(circuitBreakerContext);
    }

    @Override
    public boolean setFailed(Throwable throwable) {
        return getCircuitBreaker().setFailed(throwable);
    }

    @Override
    public boolean setFailed(Throwable throwable, CircuitBreakerContext circuitBreakerContext) {
        return getCircuitBreaker(circuitBreakerContext).setFailed(throwable, circuitBreakerContext);
    }

    @Override
    public void complete() {
        getCircuitBreaker().complete();
    }

    @Override
    public void setCircuitBreakerListener(CircuitBreakerListener... circuitBreakerListeners) {
        this.normalCircuitBreaker.setCircuitBreakerListener(circuitBreakerListeners);
        this.circuitBreakerListeners = circuitBreakerListeners;
    }

    @Override
    public boolean isFallbackOnException() {
        return getCircuitBreaker().isFallbackOnException();
    }

    @Override
    public CircuitBreakerContext getCircuitBreakerContext() {
        return getCircuitBreaker().getCircuitBreakerContext();
    }

    @Override
    public void markReject() {
        getCircuitBreaker().markReject();
    }

    @Override
    public boolean isDefaultDegrade() {
        return getCircuitBreaker().isDefaultDegrade();
    }

    @Override
    public Object handleDegrade() throws Exception {
        return getCircuitBreaker().handleDegrade();
    }

    @Override
    public CircuitBreakerProperties getCircuitBreakerProperties() {
        return circuitBreakerProperties;
    }
}
