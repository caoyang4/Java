package src.rhino;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import com.mysql.cj.util.StringUtils;

import src.rhino.annotation.Degrade;
import src.rhino.annotation.NoopClass;
import src.rhino.annotation.RateLimit;
import src.rhino.annotation.ThreadPoolExecute;
import src.rhino.cache.Cache;
import src.rhino.cache.CacheProperties;
import src.rhino.cache.DefaultCacheProperties;
import src.rhino.circuit.CircuitBreaker;
import src.rhino.circuit.CircuitBreakerListener;
import src.rhino.circuit.DefaultCircuitBreakerProperties;
import src.rhino.exception.RhinoRuntimeException;
import src.rhino.fault.DefaultFaultInjectProperties;
import src.rhino.fault.FaultInject;
import src.rhino.fault.FaultInjectProperties;
import src.rhino.limit.DefaultRequestLimiterProperties;
import src.rhino.limit.RequestLimiter;
import src.rhino.limit.RequestLimiterProperties;
import src.rhino.log.Logger;
import src.rhino.log.LoggerFactory;
import src.rhino.retry.DefaultRetryProperties;
import src.rhino.retry.Retry;
import src.rhino.retry.listener.RetryListener;
import src.rhino.threadpool.DefaultThreadPoolProperties;
import src.rhino.threadpool.ThreadPool;
import src.rhino.threadpool.ThreadPoolProperties;
import src.rhino.util.StringHelper;

/**
 * @author zhanjun on 2017/4/20.
 */
public class RhinoMethodFactory {

    private static final Logger logger = LoggerFactory.getLogger(RhinoMethodFactory.class);
    private static Map<String, RhinoMethod> rhinoMethodMap = new ConcurrentHashMap<>();

    /**
     * @param clazz
     * @param method
     * @return
     */
    public static void init(Class clazz, Method method, Object origin) {
        if (!hasRhinoAnnotation(method) || ignoreMethod(method)) {
            return;
        }
        String key = StringHelper.generateKey(clazz, method);
        RhinoMethod rhinoMethod = rhinoMethodMap.get(key);
        if (rhinoMethod == null) {
            synchronized (method) {
                rhinoMethod = rhinoMethodMap.get(key);
                if (rhinoMethod == null) {
                    Class returnType = method.getReturnType();
                    rhinoMethod = new RhinoMethod(clazz, method, origin);
                    CircuitBreaker circuitBreaker = parseDegrade(key, method, rhinoMethod);
                    rhinoMethod.setCircuitBreaker(circuitBreaker);

                    RequestLimiter requestLimiter = parseLimit(key, method, rhinoMethod);
                    rhinoMethod.setRequestLimiter(requestLimiter);

                    FaultInject requestInject = parseFaultInject(key, method, rhinoMethod);
                    rhinoMethod.setFaultInject(requestInject);

                    ThreadPool threadPool = parseThreadPool(key, method);
                    rhinoMethod.setThreadPool(threadPool);

                    Cache cache = parseCache(method, rhinoMethod);
                    rhinoMethod.setCache(cache);

                    Retry retry = parseRetry(key, method, rhinoMethod);
                    rhinoMethod.setRetry(retry);

                    rhinoMethod.setAsync(Future.class.isAssignableFrom(returnType));
                    rhinoMethodMap.put(key, rhinoMethod);
                }
            }
        }
    }

    /**
     * @param key
     * @param method
     * @return
     */
    private static CircuitBreaker parseDegrade(String key, Method method, RhinoMethod rhinoMethod) {
        Degrade degrade = method.getAnnotation(Degrade.class);
        if (degrade == null) {
            return CircuitBreaker.Factory.getEmpty();
        }

        String rhinoKey = degrade.rhinoKey();
        if (StringUtils.isNullOrEmpty(rhinoKey)) {
            throw new RuntimeException("CircuitBreaker rhino key can not be blank, please give a meaningful name");
        }

        //降级方法
        String fallbackMethodName = degrade.fallBackMethod();
        Method fallbackMethod = findMethod(rhinoMethod.getMethod().getDeclaringClass(), fallbackMethodName, method.getParameterTypes());
        rhinoMethod.setDegradeFallbackMethod(fallbackMethod);

        Class<?>[] parameterTypes = fallbackMethod.getParameterTypes();
        if (parameterTypes.length > 0) {
            Class<?> lastParameter = parameterTypes[parameterTypes.length - 1];
            if (Throwable.class.isAssignableFrom(lastParameter)) {
                rhinoMethod.setFallbackMethod_has_throwable_param(true);
            }
        }

        DefaultCircuitBreakerProperties.Setter setter = new DefaultCircuitBreakerProperties.Setter(degrade);
        CircuitBreaker circuitBreaker = CircuitBreaker.Factory.getInstance(rhinoKey, setter, RhinoUseMode.ANNOTATION);

        // 熔断回调函数
        Class<? extends CircuitBreakerListener> clazz = degrade.circuitBreakerListener();
        try {
            CircuitBreakerListener circuitBreakerListener = clazz.newInstance();
            circuitBreaker.setCircuitBreakerListener(circuitBreakerListener);
        } catch (Exception e) {
            logger.warn(key + " circuitBreakerListener init failed ", e);
        }
        return circuitBreaker;
    }

    /**
     * @param key
     * @param method
     * @return
     */
    private static RequestLimiter parseLimit(String key, Method method, RhinoMethod rhinoMethod) {
        RateLimit limit = method.getAnnotation(RateLimit.class);
        if (limit == null) {
            return RequestLimiter.Factory.getEmpty();
        }

        String rhinoKey = limit.rhinoKey();
        if (StringUtils.isNullOrEmpty(rhinoKey)) {
            throw new RuntimeException("RequestLimiter rhino key can not be blank, please give a meaningful name");
        }

        String fallbackMethodName = limit.fallBackMethod();
        if (!fallbackMethodName.isEmpty()) {
            Method fallbackMethod = findMethod(rhinoMethod.getMethod().getDeclaringClass(), fallbackMethodName, method.getParameterTypes());
            rhinoMethod.setLimiterFallbackMethod(fallbackMethod);
        }

        RequestLimiterProperties requestLimiterProperties = new DefaultRequestLimiterProperties(limit);
        RequestLimiter requestLimiter = RequestLimiter.Factory.getInstance(rhinoKey, requestLimiterProperties, RhinoUseMode.ANNOTATION.getValue());
        return requestLimiter;
    }

    /**
     * @param key
     * @param method
     * @return
     */
    private static FaultInject parseFaultInject(String key, Method method, RhinoMethod rhinoMethod) {
        src.rhino.annotation.FaultInject fault = method.getAnnotation(src.rhino.annotation.FaultInject.class);
        if (fault == null) {
            return FaultInject.Factory.getEMPTY();
        }
        String rhinoKey = fault.rhinoKey();
        if (StringUtils.isNullOrEmpty(rhinoKey)) {
            throw new RuntimeException("FaultInject rhino key can not be blank, please give a meaningful name");
        }

        Class<?> returnType = fault.mockType();
        if (returnType == NoopClass.class) {
            rhinoMethod.setReturnType(method.getReturnType());
        } else {
            rhinoMethod.setReturnType(returnType);
        }

        FaultInjectProperties injectProperties = new DefaultFaultInjectProperties(fault);
        FaultInject faultInject = FaultInject.Factory.getInstance(rhinoKey, injectProperties, RhinoUseMode.ANNOTATION.getValue());
        return faultInject;
    }

    /**
     * @param method
     * @return
     */
    private static Cache parseCache(Method method, RhinoMethod rhinoMethod) {
        src.rhino.annotation.Cache cache = method.getAnnotation(src.rhino.annotation.Cache.class);
        if (cache == null) {
            return null;
        }

        String cacheKeyMethodName = cache.cacheKeyMethod();
        Method cacheKeyMethod = findMethod(rhinoMethod.getMethod().getDeclaringClass(), cacheKeyMethodName, method.getParameterTypes());
        rhinoMethod.setCacheKeyMethod(cacheKeyMethod);

        String rhinoKey = cache.rhinoKey();
        if (StringUtils.isNullOrEmpty(rhinoKey)) {
            throw new RuntimeException("cache rhino key can not be blank, please give a meaningful name");
        }
        CacheProperties properties = new DefaultCacheProperties(cache);
        return Cache.Factory.getInstance(rhinoKey, properties, RhinoUseMode.ANNOTATION.getValue());
    }

    /**
     * @param key
     * @param method
     * @return
     */
    private static ThreadPool parseThreadPool(String key, Method method) {
        ThreadPoolExecute threadPoolExecute = method.getAnnotation(ThreadPoolExecute.class);
        if (threadPoolExecute == null) {
            return ThreadPool.Factory.getEmpty();
        }

        String rhinoKey = threadPoolExecute.rhinoKey();
        if (StringUtils.isNullOrEmpty(rhinoKey)) {
            throw new RuntimeException("ThreadPool rhino key can not be blank, please give a meaningful name");
        }
        DefaultThreadPoolProperties.Setter setter = new DefaultThreadPoolProperties.Setter(threadPoolExecute);
        ThreadPoolProperties threadPoolProperties = new DefaultThreadPoolProperties(rhinoKey, setter);
        return ThreadPool.Factory.getInstance(rhinoKey, threadPoolProperties, RhinoUseMode.ANNOTATION);
    }


    private static Retry parseRetry(String key, Method method, RhinoMethod rhinoMethod) {
        src.rhino.annotation.Retry retry = method.getAnnotation(src.rhino.annotation.Retry.class);
        if (retry == null) {
            return Retry.Factory.getEMPTY();
        }
        String rhinoKey = retry.rhinoKey();
        if (StringUtils.isNullOrEmpty(rhinoKey)) {
            throw new RuntimeException("Retry rhino key can not be blank, please give a meaningful name");
        }
        //恢复方法
        String recoverMethodName = retry.recoverMethod();
        if (!StringUtils.isNullOrEmpty(recoverMethodName)) {
            Method recoverMethod = null;
            Class<?> target = rhinoMethod.getMethod().getDeclaringClass();
            try {
                recoverMethod = findMethod(target, recoverMethodName, method.getParameterTypes());
            } catch (RhinoRuntimeException e) {
                logger.warn(e.getMessage());
            }
            rhinoMethod.setRetryRecoverMethod(recoverMethod);
        }
        DefaultRetryProperties.Setter setter = new DefaultRetryProperties.Setter(retry);
        Retry defaultRetry = Retry.Factory.getInstance(rhinoKey, setter, RhinoUseMode.ANNOTATION);
        Class<? extends RetryListener>[] retryListenerClasses = retry.retryListeners();
        if (retryListenerClasses.length > 0) {
            for (Class<? extends RetryListener> retryListenerClass : retryListenerClasses) {
                try {
                    RetryListener listener = retryListenerClass.newInstance();
                    defaultRetry.addRetryListener(listener);
                } catch (Exception e) {
                    logger.warn("fail initial Retry listener(" + retryListenerClass + ")", e);
                }
            }
        }
        return defaultRetry;
    }

    /**
     * @param method
     * @return
     */
    private static boolean hasRhinoAnnotation(Method method) {
        Annotation degrade = method.getAnnotation(Degrade.class);
        Annotation rateLimit = method.getAnnotation(RateLimit.class);
        Annotation inject = method.getAnnotation(src.rhino.annotation.FaultInject.class);
        Annotation threadPoolExecute = method.getAnnotation(ThreadPoolExecute.class);
        Annotation cache = method.getAnnotation(src.rhino.annotation.Cache.class);
        Annotation retry = method.getAnnotation(src.rhino.annotation.Retry.class);
        return degrade != null || rateLimit != null || inject != null || threadPoolExecute != null || cache != null || retry != null;
    }

    /**
     * 4096 (0x1000) indicates a synthetic method,
     * i.e. a method that is not present in the source code.
     * 64 (0x0040) not only represents the volatile access modifier, but can also be used to signify that a method is a bridge method,
     * i.e. a method that is generated by the compiler.
     * 去除编译器生成的方法，如范型继承产生的桥接方法
     *
     * @param method
     * @return
     */
    private static boolean ignoreMethod(Method method) {
        return method.isBridge() || method.isSynthetic();
    }

    /**
     * @param target
     * @param methodName
     * @param pTypes
     * @return
     */
    private static Method findMethod(Class<?> target, String methodName, Class<?>... pTypes) {
        if (!StringUtils.isNullOrEmpty(methodName)) {
            for (Method method : target.getDeclaredMethods()) {
                if (method.getName().equals(methodName)) {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if (pTypes.length == 0 && parameterTypes.length == 0) {
                        method.setAccessible(true);
                        return method;
                    }

                    Class<?>[] origParamTypes = parameterTypes;
                    int gap = parameterTypes.length - pTypes.length;
                    if (gap == 1 || gap == 0) {
                        if (gap == 1) {
                            Class<?> lastParameter = parameterTypes[parameterTypes.length - 1];
                            // 最后一个必须是异常类型
                            if (!Throwable.class.isAssignableFrom(lastParameter)) {
                                continue;
                            }
                            origParamTypes = removeLastParameter(parameterTypes);
                        }
                        boolean match = true;
                        int index = 0;
                        if (origParamTypes != null) {
                            for (Class<?> pType : origParamTypes) {
                                Class<?> expected = pTypes[index++];
                                if (pType != expected) {
                                    match = false;
                                    break;
                                }
                            }
                        }
                        if (match) {
                            method.setAccessible(true);
                            return method;
                        }
                    }
                }
            }
        }

        // 从父类寻找
        if (target.getSuperclass() != null) {
            return findMethod(target.getSuperclass(), methodName, pTypes);
        }

        throw new RhinoRuntimeException("can not find fallBack method " + methodName +
                StringHelper.genMethodParameter(pTypes) +
                " in " +
                target);
    }

    /**
     * 删除最后一个元素
     *
     * @param parameterTypes
     * @return
     */
    private static Class<?>[] removeLastParameter(Class<?>[] parameterTypes) {
        if (parameterTypes.length > 0) {
            Class<?>[] origParamTypes = new Class[parameterTypes.length - 1];
            System.arraycopy(parameterTypes, 0, origParamTypes, 0, parameterTypes.length - 1);
            return origParamTypes;
        }
        return new Class[0];
    }

    public static RhinoMethod getMethod(String key) {
        return rhinoMethodMap.get(key);
    }
}
