package src.rhino;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import src.rhino.circuit.CircuitBreaker;
import src.rhino.circuit.CircuitBreakerContext;
import src.rhino.circuit.DefaultCircuitBreakerProperties;
import src.rhino.circuit.NoOpCircuitBreaker;
import src.rhino.circuit.RequestStatus;
import src.rhino.circuit.timeout.TimeoutStatus;
import src.rhino.exception.RhinoRuntimeException;
import src.rhino.threadpool.DefaultThreadPoolProperties;
import src.rhino.threadpool.ThreadPool;
import src.rhino.util.CommonUtils;
import src.rhino.util.MtraceUtils;

/**
 * Created by zhanjun on 2018/8/9.
 */
public abstract class RhinoAbstractCommand<R> {

    protected AtomicBoolean commandStart = new AtomicBoolean(false);
    protected CircuitBreakerContext context = new CircuitBreakerContext(MtraceUtils.isTest());
    protected CircuitBreaker circuitBreaker;
    protected ThreadPool threadPool;

    public RhinoAbstractCommand(CircuitBreaker circuitBreaker, ThreadPool threadPool) {
        this.circuitBreaker = circuitBreaker;
        this.threadPool = threadPool;
    }

    public RhinoAbstractCommand(String rhinoKey, String threadPoolKey, DefaultCircuitBreakerProperties.Setter circuitBreakerProperties, DefaultThreadPoolProperties.Setter threadPoolProperties) {
        this.circuitBreaker = Rhino.newCircuitBreaker(rhinoKey, circuitBreakerProperties);
        this.threadPool = Rhino.newThreadPool(threadPoolKey != null ? threadPoolKey : rhinoKey, threadPoolProperties);
    }

    /**
     * 信号量模式
     * 执行时没有线程上下文的切换
     *
     * @return R
     * @throws Exception
     */
    protected R execute() throws Exception {
        markCommandStart();
        boolean allow = circuitBreaker.allowRequest(context);
        if (allow) {
            return doRun();
        } else {
            return handleCircuitViaFallback();
        }
    }

    /**
     * 正常逻辑
     * 发生异常时，执行降级逻辑
     * 1、试探请求失败
     * 2、正常请求失败，前提是degradeOnException开启
     *
     * @return
     * @throws Exception
     */
    private R doRun() throws Exception {
        try {
            R result = run();
            circuitBreaker.setSuccess(context);
            return result;
        } catch (Throwable t) {
            Exception actualException = CommonUtils.getActualException(t);
            circuitBreaker.setFailed(actualException, context);
            RequestStatus status = context.getRequestStatus();
            if (status.isSingleTest() || circuitBreaker.isFallbackOnException()) {
                return handleFailureViaFallback(actualException);
            }
            throw actualException;
        }
    }

    /**
     * 线程池模式
     *
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    protected Future<R> executeAsync() throws Exception {
        markCommandStart();
        Future<R> innerFuture = null;
        R innerValue = null;
        Exception fallbackException = null;
        boolean allow = circuitBreaker.allowRequest(context);
        if (allow) {
            try {
                // circuitBreakerContext初始化时，变量currentThread为当前线程
                // 如果任务一直堵在队列里面，被超时检测判断为超时，当前线程可能被设置中断标识
                // future.get响应中断，抛出InterruptedException异常，并清除中断标识
                innerFuture = threadPool.submit(new Callable<R>() {
                    @Override
                    public R call() throws Exception {
                        context.setCurrentThread();
                        return doRun();
                    }
                });
            } catch (RejectedExecutionException e) {
                // 线程池拒绝，抛出RejectedExecutionException
                try {
                    innerValue = handleThreadPoolRejectionViaFallback(e);
                } catch (Exception e1) {
                    fallbackException = e1;
                }
            }
        } else {
            try {
                innerValue = handleCircuitViaFallback();
            } catch (Exception e) {
                fallbackException = e;
            }
        }

        final Future<R> future = innerFuture;
        final R value = innerValue;
        final Exception exception = fallbackException;

        return new Future<R>() {
            @Override
            public R get() throws InterruptedException, ExecutionException {
                try {
                    return get(-1, TimeUnit.MILLISECONDS);
                } catch (TimeoutException e) {
                    throw new ExecutionException(e);
                }
            }

            @Override
            public R get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                if (future == null) {
                    if (exception != null) {
                        throw new ExecutionException(exception);
                    }
                    return value;
                }

                try {
                    if (timeout < 0) {
                        return future.get();
                    }
                    return future.get(timeout, unit);
                } catch (Throwable t) {
                    // future.get(timeout, unit)方法抛出的异常可能如下：
                    // 1、TimeoutException : get方法超时
                    // 3、InterruptedException : 返回的Future被cancel、或者提交任务还在队列中就已经超时
                    // 2、ExecutionException : isDegradeOnException为false，子线程发生异常时没有执行降级，重新抛出异常
                    Exception actualException = CommonUtils.getActualException(t);
                    if (circuitBreaker.setFailed(actualException, context)) {
                        future.cancel(true);
                        // 判断熔断之前的异常是否走降级逻辑
                        // 默认抛出异常，fail-fast，可以让业务尽早发现问题
                        if (circuitBreaker.isFallbackOnException()) {
                            try {
                                return handleFailureViaFallback(actualException);
                            } catch (Exception e) {
                                actualException = e;
                            }
                        }
                    }

                    if (actualException instanceof ExecutionException) {
                        throw (ExecutionException) actualException;
                    }

                    // 其它情况
                    throw new ExecutionException(actualException);
                }
            }

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return future != null && future.cancel(mayInterruptIfRunning);
            }

            @Override
            public boolean isCancelled() {
                return future != null && future.isCancelled();
            }

            @Override
            public boolean isDone() {
                return future == null || future.isDone();
            }
        };
    }

    /**
     * 一个command只能被执行一次
     *
     * @throws Exception
     */
    private void markCommandStart() throws Exception {
        if (!commandStart.compareAndSet(false, true)) {
            IllegalStateException ex = new IllegalStateException("This command instance can only be execute once. Please instantiate a new instance.");
            throw new RhinoRuntimeException(ex);
        }
    }

    /**
     * 熔断之后降级
     *
     * @return
     * @throws Exception
     */
    private R handleCircuitViaFallback() throws Exception {
        try {
            return doFallback(null);
        } catch (Throwable t) {
            throw CommonUtils.getActualException(t);
        }
    }

    /**
     * 正常请求发生异常降级
     *
     * @param throwable
     * @return
     * @throws Exception
     */
    private R handleFailureViaFallback(Throwable throwable) throws Exception {
        try {
            return doFallback(throwable);
        } catch (Throwable t) {
            throw CommonUtils.getActualException(t);
        }
    }

    /**
     * 线程池异常降级
     *
     * @param throwable
     * @return
     * @throws Exception
     */
    private R handleThreadPoolRejectionViaFallback(Throwable throwable) throws Exception {
        if (circuitBreaker instanceof NoOpCircuitBreaker) {
            //如果只接了线程池没有熔断器，直接抛出
            throw CommonUtils.getActualException(throwable);
        }

        circuitBreaker.markReject();
        try {
            return doFallback(throwable);
        } catch (Throwable t) {
            throw CommonUtils.getActualException(t);
        }
    }

    /**
     * 降级执行逻辑
     *
     * @return
     * @throws Exception
     */
    public R doFallback(Throwable throwable) throws Exception {
        // 如果是因为超时执行降级，则执行降级逻辑之前清空中断标识
        if (context.getTimeoutStatus() == TimeoutStatus.TIMED_OUT) {
            Thread.interrupted();
        }
        if (circuitBreaker.isDefaultDegrade()) {
            return getFallback(throwable);
        }
        return (R) circuitBreaker.handleDegrade();
    }

    /**
     * 正常执行逻辑
     *
     * @return
     * @throws Exception
     */
    public abstract R run() throws Exception;

    /**
     * 降级执行逻辑
     *
     * @return
     * @throws Exception
     */
    public abstract R getFallback(Throwable throwable) throws Exception;
}
