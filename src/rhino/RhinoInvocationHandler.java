package src.rhino;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import src.rhino.limit.RequestLimiter;
import src.rhino.retry.RecoverCallback;
import src.rhino.retry.Retry;
import src.rhino.retry.RetryCallback;
import src.rhino.util.CommonUtils;
import src.rhino.util.StringHelper;

/**
 * @author zhanjun on 2017/4/20.
 */
public class RhinoInvocationHandler implements InvocationHandler, MethodInterceptor {

    private Object origin;
    private Class clazz;

    public RhinoInvocationHandler(Object origin, Class clazz) {
        this.origin = origin;
        this.clazz = clazz;
    }

    /**
     * 有接口实现的服务，采用JDK实现代理
     *
     * @param proxy
     * @param method
     * @param args
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if(args == null) {
        	args = new Object[0];
        }
    	return execute(method, args);
    }

    /**
     * 没有接口实现的服务，采用CGLIB实现代理
     *
     * @param o
     * @param method
     * @param args
     * @param methodProxy
     * @return
     * @throws Throwable
     */
    @Override
    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        return execute(method, args);
    }

    /**
     * limiter -> retry -> circuit breaker -> fault inject
     *
     * @param args
     * @param method
     * @return
     * @throws Throwable
     */
    private Object execute(Method method, final Object[] args) throws Throwable {
        String key = StringHelper.generateKey(clazz, method);
        final RhinoMethod rhinoMethod = RhinoMethodFactory.getMethod(key);

        if (rhinoMethod == null) {
            try {
                return method.invoke(origin, args);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }

        //limiter
        RequestLimiter requestLimiter = rhinoMethod.getRequestLimiter();
        boolean needDegrade = requestLimiter.isNeedDegrade();
        // 请求被限流，选择降级处理
        if (needDegrade) {
            final Object value;
            try {
                value = rhinoMethod.limiterFallback(origin, args);
            } catch (Throwable t) {
                throw CommonUtils.getActualException(t);
            }
            if (rhinoMethod.getThreadPool() != null && rhinoMethod.isAsync()) {
                return new Future<Object>() {
                    @Override
                    public boolean cancel(boolean mayInterruptIfRunning) {
                        return false;
                    }

                    @Override
                    public boolean isCancelled() {
                        return false;
                    }

                    @Override
                    public boolean isDone() {
                        return true;
                    }

                    @Override
                    public Object get() throws InterruptedException, ExecutionException {
                        return value;
                    }

                    @Override
                    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                        return value;
                    }
                };
            }
            return value;
        }
        // retry wrapper
        Retry retry = rhinoMethod.getRetry();
        RecoverCallback<Object> recoverCallback = null;
        if (rhinoMethod.getRetryRecoverMethod() != null) {
            recoverCallback = new RecoverCallback<Object>() {
                @Override
                public Object recover() throws Throwable {
                    return rhinoMethod.retryRecover(origin, args);
                }
            };
        }
        return retry.execute(new RetryCallback<Object, Throwable>() {

            @Override
            public Object doWithRetry() throws Throwable {
                RhinoInnerCommand innerCommand = new RhinoInnerCommand(origin, rhinoMethod, args);

                // 信号量模式执行
                if (rhinoMethod.getThreadPool() == null) {
                    return innerCommand.execute();
                }

                // 线程池模式执行
                // 根据返回参数进行判断
                Future future = innerCommand.executeAsync();
                if(rhinoMethod.isAsync()){
                    //异步模式下直接返回future
                    return future;
                }

                try{
                    return future.get();
                }catch (ExecutionException executeException){
                    //同步模式下，如果发生业务异常，就解析处理一下
                    throw executeException.getCause();
                }catch (Exception e){
                    //非业务异常（比如中断），不做处理直接抛出
                    throw e;
                }
            }
        }, recoverCallback, args);
    }
}
