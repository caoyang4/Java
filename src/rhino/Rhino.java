package src.rhino;

import src.rhino.circuit.CircuitBreaker;
import src.rhino.circuit.CircuitBreakerProperties;
import src.rhino.circuit.DefaultCircuitBreakerProperties;
import src.rhino.exception.RhinoRuntimeException;
import src.rhino.fault.FaultInject;
import src.rhino.fault.FaultInjectProperties;
import src.rhino.limit.RequestLimiter;
import src.rhino.limit.RequestLimiterProperties;
import src.rhino.onelimiter.OneLimiter;
import src.rhino.retry.DefaultRetryProperties;
import src.rhino.retry.Retry;
import src.rhino.service.RhinoManager;
import src.rhino.threadpool.DefaultThreadPoolProperties;
import src.rhino.threadpool.ThreadPool;
import src.rhino.threadpool.ThreadPoolProperties;
import src.rhino.util.AppUtils;
import src.rhino.util.PropertiesHolder;

/**
 * @author zhanjun on 2017/4/20.
 */
public final class Rhino {

    public static final String VERSION = PropertiesHolder.getVersion();

    static {
        RhinoManager.addHook();
    }

    private Rhino() {
        throw new IllegalStateException("can't init with constructor");
    }

    /**
     * 手动设置appKey
     *
     * @param appKey
     */
    public static void initializeAppKeyForce(String appKey) {
        AppUtils.initializeAppKeyForce(appKey);
    }

    /**
     * @param key
     * @return
     */
    public static CircuitBreaker newCircuitBreaker(String key) {
        return newCircuitBreaker(key, false);
    }

    /**
     * @param key
     * @param active
     * @return
     */
    public static CircuitBreaker newCircuitBreaker(String key, boolean active) {
        return CircuitBreaker.Factory.getInstance(key, active);
    }

    /**
     * 如果需要自定义参数，使用Setter
     *
     * @param key
     * @param setter
     * @return
     */
    public static CircuitBreaker newCircuitBreaker(String key, DefaultCircuitBreakerProperties.Setter setter) {
        return CircuitBreaker.Factory.getInstance(key, setter);
    }

    /**
     * 不建议使用此方法
     *
     * @param key
     * @param circuitBreakerProperties
     * @return
     */
    @Deprecated
    public static CircuitBreaker newCircuitBreaker(String key, CircuitBreakerProperties circuitBreakerProperties) {
        throw new RhinoRuntimeException("该方法已废弃，请使用DefaultCircuitBreakerProperties.Setter方式进行参数设置");
    }

    /**
     * RPC客户端初始化熔断器
     * @param remoteAppkey 服务端appkey
     * @param service  类名（包全名）
     * @param method  方法名
     * @return
     */
    public static CircuitBreaker getCircuitBreaker(String remoteAppkey, String service, String method){
        return CircuitBreaker.Factory.getCircuitBreaker(remoteAppkey, service, method);
    }

    /**
     * @param key
     * @return
     */
    public static ThreadPool newThreadPool(String key) {
        return newThreadPool(key, DefaultThreadPoolProperties.Setter());
    }

    /**
     * @param key
     * @return
     */
    public static ThreadPool newThreadPool(String key, DefaultThreadPoolProperties.Setter setter) {
        return ThreadPool.Factory.getInstance(key, new DefaultThreadPoolProperties(key, setter));
    }

    /**
     * 不建议使用此方法
     *
     * @param key
     * @param threadPoolProperties
     * @return
     */
    @Deprecated
    public static ThreadPool newThreadPool(String key, ThreadPoolProperties threadPoolProperties) {
        return ThreadPool.Factory.getInstance(key, threadPoolProperties);
    }

    /**
     * 创建单个限流器
     *
     * @param key
     * @return
     */
    public static RequestLimiter newRequestLimiter(String key) {
        return newRequestLimiter(key, null);
    }

    /**
     * @param key
     * @param requestLimiterProperties
     * @return
     */
    public static RequestLimiter newRequestLimiter(String key, RequestLimiterProperties requestLimiterProperties) {
        return RequestLimiter.Factory.getInstance(key, requestLimiterProperties);
    }

    /**
     * @param key
     * @return
     */
    public static RequestLimiter newRequestLimiterGroup(String key) {
        return newRequestLimiterGroup(key, null);
    }

    /**
     * @param key
     * @param requestLimiterProperties
     * @return
     */
    public static RequestLimiter newRequestLimiterGroup(String key, RequestLimiterProperties requestLimiterProperties) {
        return RequestLimiter.Factory.getGroupInstance(key, requestLimiterProperties);
    }

    /**
     * @param key
     * @return
     */
    public static RequestLimiter newFeatureRequestLimiter(String key) {
        return RequestLimiter.Factory.getFeatureInstance(key, null);
    }

    /**
     * @return
     */
    public static OneLimiter newOneLimiter() {
        return newOneLimiter("rhino-one-limiter");
    }

    /**
     * @param key
     * @return
     */
    public static OneLimiter newOneLimiter(String key) {
        return OneLimiter.Factory.getInstance(key);
    }

    /**
     * 创建故障演练
     *
     * @param key
     * @return
     */
    public static FaultInject newFaultInject(String key) {
        return FaultInject.Factory.getInstance(key, null);
    }

    /**
     * 创建故障演练
     *
     * @param key
     * @param faultInjectProperties
     * @return
     */
    public static FaultInject newFaultInject(String key, FaultInjectProperties faultInjectProperties) {
        return FaultInject.Factory.getInstance(key, faultInjectProperties);
    }

    public static Retry newRetry(String key) {
        return Retry.Factory.getInstance(key);
    }

    public static Retry newRetry(String key, boolean active) {
        return Retry.Factory.getInstance(key, new DefaultRetryProperties.Setter().withActive(active));
    }

    public static Retry newRetry(String key, DefaultRetryProperties.Setter setter) {
        return Retry.Factory.getInstance(key, setter);
    }

}
