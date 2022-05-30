package src.rhino.circuit;

import src.rhino.RhinoType;
import src.rhino.dispatcher.RhinoEventType;

/**
 * @author zhanjun
 */
public enum CircuitBreakerEventType implements RhinoEventType {

    /**
     * switch.open
     * 熔断自动开启事件
     */
    CIRCUIT_BREAKER_AUTO_OPEN(1, "circuitbreaker.auto.open", true),

    /**
     * switch.close
     * 熔断自动关闭事件
     */
    CIRCUIT_BREAKER_AUTO_CLOSE(2, "circuitbreaker.auto.close", true),

    /**
     * 熔断手动开启事件
     */
    CIRCUIT_BREAKER_FORCE_OPEN(3, "circuitbreaker.force.open", true),

    /**
     * 熔断手动关闭事件
     */
    CIRCUIT_BREAKER_FORCE_CLOSE(4, "circuitbreaker.force.close", true),

    /**
     * 正常请求
     * 成功
     */
    REQUEST_NORMAL_SUCCESS(5, "request.normal.success", false),

    /**
     * 正常请求
     * 失败
     */
    REQUEST_NORMAL_FAILED(5, "request.normal.failed", false),

    /**
     * 直接降级请求
     */
    REQUEST_DEGRADE(6, "request.degrade", false),

    /**
     * 请求超时
     */
    REQUEST_TIME_OUT(7, "request.timeout", false),

    /**
     * 试探请求成功
     */
    HEARTBEAT_SUCCESS(8, "heartbeat.success", false),

    /**
     * 试探请求失败
     */
    HEARTBEAT_FAILED(9, "heartbeat.failed", false),

    /**
     * 失败降级请求
     */
    REQUEST_FAILED_DEGREE(10, "request.failed.degrade", false),

    /**
     * 请求被线程池拒绝
     */
    REQUEST_REJECT(11, "request.reject", false),

    /**
     * 请求被信号量拒绝
     */
    REQUEST_REJECT_SEMAPHORE(12, "request.reject.semaphore", false);


    private String type = "Rhino.CircuitBreaker";
    private int index;
    private String value;
    private boolean isNotify;

    CircuitBreakerEventType(int type, String value, boolean isNotify) {
        this.index = type;
        this.value = value;
        this.isNotify = isNotify;
    }

    @Override
    public RhinoType getRhinoType() {
        return RhinoType.CircuitBreaker;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public boolean isNotify() {
        return isNotify;
    }
}
