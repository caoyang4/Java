package src.rhino;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.springframework.aop.support.AopUtils;

import src.rhino.cache.Cache;
import src.rhino.circuit.CircuitBreaker;
import src.rhino.fault.FaultInject;
import src.rhino.limit.RequestLimiter;
import src.rhino.retry.Retry;
import src.rhino.threadpool.ThreadPool;

/**
 * @author zhanjun on 2017/4/20.
 */
public class RhinoMethod {
    private Method method;
    private CircuitBreaker circuitBreaker;
    private Method degradeFallbackMethod;
    private Method limiterFallbackMethod;
    private Method retryRecoverMethod;
    private RequestLimiter requestLimiter;
    private FaultInject faultInject;
    private ThreadPool threadPool;
    private Retry retry;
    private Cache cache;
    private Method cacheKeyMethod;
    private Class<?> returnType;
    private boolean isAsync;
    private boolean fallbackMethod_has_throwable_param;

    public RhinoMethod(Class clazz, Method method, Object origin) {
        this.method = checkMethod(clazz, method, origin);
    }

    /**
     * 如果被代理的对象是TargetClassAware，
     * 即 Implemented by AOP proxy objects and proxy factories
     * 那么执行method.invoke()会抛异常，因为该method不是实现类的方法对象，
     * 而被代理的对象并非该实现类的实例，而是实现类接口的实例，所以需要获得接口的method对象
     * @param clazz
     * @param method
     * @return
     */
    private static Method checkMethod(Class clazz, Method method, Object origin) {
        if(AopUtils.isJdkDynamicProxy(origin)){
            //动态代理需要追溯到接口，cglib代理不需要
            Class<?>[] interfaces = clazz.getInterfaces();
            if (interfaces != null && interfaces.length > 0) {
                for (int index = interfaces.length - 1; index >= 0; index--) {
                    Class<?> clazz0 = interfaces[index];
                    try {
                        return clazz0.getMethod(method.getName(), method.getParameterTypes());
                    } catch (NoSuchMethodException e) {
                        //ignore exception
                    }
                }
            }
        }
        return method;
    }

    /**
     * 限流之后的降级
     * @param origin
     * @param arguments
     * @return
     * @throws Exception
     */
    public Object limiterFallback(Object origin, Object[] arguments) throws Exception {
        if (limiterFallbackMethod == null) {
            return null;
        }
        return limiterFallbackMethod.invoke(origin, arguments);
    }

    /**
     * retry recover
     * @param origin
     * @param arguments
     * @return
     * @throws Exception
     */
    public Object retryRecover(Object origin, Object[] arguments) throws Throwable{
        if(retryRecoverMethod==null){
            return null;
        }
        return retryRecoverMethod.invoke(origin,arguments);
    }


    /**
     *
     * @param origin
     * @param arguments
     * @param result
     * @throws Exception
     */
    public void putInCache(Object origin, Object[] arguments, Object result) throws Exception {
        if (cache != null && cacheKeyMethod != null) {
            Object cacheKey = cacheKeyMethod.invoke(origin, arguments);
            cache.put(cacheKey, result);
        }
    }

    /**
     * 调用正常方法
     * @param origin
     * @param arguments
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public Object invoke(Object origin, Object[] arguments) throws InvocationTargetException, IllegalAccessException, InterruptedException {
        return method.invoke(origin, arguments);
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public CircuitBreaker getCircuitBreaker() {
        return circuitBreaker;
    }

    public void setCircuitBreaker(CircuitBreaker circuitBreaker) {
        this.circuitBreaker = circuitBreaker;
    }

    public RequestLimiter getRequestLimiter() {
        return requestLimiter;
    }

    public void setRequestLimiter(RequestLimiter requestLimiter) {
        this.requestLimiter = requestLimiter;
    }

    public FaultInject getFaultInject() {
        return faultInject;
    }

    public void setFaultInject(FaultInject faultInject) {
        this.faultInject = faultInject;
    }

    public Method getDegradeFallbackMethod() {
        return degradeFallbackMethod;
    }

    public void setDegradeFallbackMethod(Method degradeFallbackMethod) {
        this.degradeFallbackMethod = degradeFallbackMethod;
    }

    public Method getLimiterFallbackMethod() {
        return limiterFallbackMethod;
    }

    public void setLimiterFallbackMethod(Method limiterFallbackMethod) {
        this.limiterFallbackMethod = limiterFallbackMethod;
    }

    public ThreadPool getThreadPool() {
        return threadPool;
    }

    public void setThreadPool(ThreadPool threadPool) {
        this.threadPool = threadPool;
    }

    public Cache getCache() {
        return cache;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    public Method getCacheKeyMethod() {
        return cacheKeyMethod;
    }

    public void setCacheKeyMethod(Method cacheKeyMethod) {
        this.cacheKeyMethod = cacheKeyMethod;
    }

    public boolean isAsync() {
        return isAsync;
    }

    public void setAsync(boolean async) {
        isAsync = async;
    }

    public boolean isFallbackMethod_has_throwable_param() {
        return fallbackMethod_has_throwable_param;
    }

    public void setFallbackMethod_has_throwable_param(boolean fallbackMethod_has_throwable_param) {
        this.fallbackMethod_has_throwable_param = fallbackMethod_has_throwable_param;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public void setReturnType(Class<?> returnType) {
        this.returnType = returnType;
    }

    public Method getRetryRecoverMethod() {
        return retryRecoverMethod;
    }

    public void setRetryRecoverMethod(Method retryRecoverMethod) {
        this.retryRecoverMethod = retryRecoverMethod;
    }

    public Retry getRetry() {
        return retry;
    }

    public void setRetry(Retry retry) {
        this.retry = retry;
    }
}
