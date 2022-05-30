package src.rhino.retry;

import java.util.ArrayList;
import java.util.List;

import src.rhino.dispatcher.RhinoEvent;
import src.rhino.exception.RhinoRetryInterruptedException;
import src.rhino.retry.delay.BackOffStrategy;
import src.rhino.retry.listener.RetryListener;

/**
 * @author zhen
 * @date 2019/2/20
 */
public class DefaultRetry implements Retry {

    private volatile RetryProperties retryProperties;
    private BackOffStrategy backOffStrategy;
    private List<RetryListener> retryListeners = new ArrayList<>();
    private RetryEventDispatcher retryEventDispatcher;

    public DefaultRetry(RetryProperties retryProperties) {
        this.retryProperties = retryProperties;
        this.backOffStrategy = BackOffStrategy.Factory.create(retryProperties);
        this.retryEventDispatcher = RetryEventDispatcher.create(retryProperties.getRhinoKey());
    }

    @Override
    public <T, E extends Throwable> T execute(RetryCallback<T, E> retryCallback) throws E {
        return doExecute(retryCallback, null);
    }

    @Override
    public <T, E extends Throwable> T execute(RetryCallback<T, E> retryCallback, RecoverCallback<T> recoverCallback, Object... args) throws E {
        return doExecute(retryCallback, recoverCallback, args);
    }

    @Override
    public boolean addRetryListener(RetryListener retryListener) {
        return retryListeners.add(retryListener);
    }

    private <T, E extends Throwable> T doExecute(RetryCallback<T, E> retryCallback, RecoverCallback<T> recoverCallback, Object... args) throws E {
        //开启
        if (retryProperties.isActive()) {
            RetryContext retryContext = new RetryContext(args);
            //允许业务对context进行加工
            onOpen(retryContext);
            try {
                while (canRetry(retryContext)) {
                    try {
                        retryEventDispatcher.dispatchEvent(new RhinoEvent(retryContext.getState().getRetryEventType()));
                        T result = retryCallback.doWithRetry();
                        retryContext.recordResultSuccess(result);
                        return result;
                    } catch (Throwable e) {
                        retryContext.recordThrowable(e);
                        onError(retryContext, e);
                        //必须仍然符合retry条件
                        if (canRetry(retryContext)) {
                            //退避策略
                            backOffStrategy.backOff(retryContext);
                        }
                    }
                }
                T recoverResult = handleRecover(recoverCallback, retryContext);
                retryContext.recordRecover(recoverResult);
                return recoverResult;
            } catch (RhinoRetryInterruptedException re) {
                retryContext.recordInterrupt();
                throw re;
            } catch (Throwable t) {
                retryContext.recordResultError();
                throw (E) t;
            } finally {
                onClose(retryContext);
                retryEventDispatcher.dispatchEvent(new RhinoEvent(retryContext.getState().getRetryEventType()));
            }
        }
        //关闭
        return retryCallback.doWithRetry();
    }

    @Override
    public boolean canRetry(RetryContext retryContext) {
        return retryProperties.canRetry(retryContext);
    }


    private <T> T handleRecover(RecoverCallback<T> recoverCallback, RetryContext retryContext) throws Throwable {
        if (recoverCallback != null) {
            return recoverCallback.recover();
        }
        throw retryContext.getLastThrowable();
    }

    /**
     * 进入retry逻辑
     *
     * @param retryContext
     */
    private void onOpen(RetryContext retryContext) {
        for (RetryListener retryListener : retryListeners) {
            retryListener.onOpen(retryContext);
        }
    }

    /**
     * 业务逻辑抛异常
     *
     * @param retryContext
     * @param t
     */
    private void onError(RetryContext retryContext, Throwable t) {
        for (RetryListener retryListener : retryListeners) {
            retryListener.onError(retryContext, t);
        }
    }

    /**
     * retry 结束
     *
     * @param retryContext
     */
    private void onClose(RetryContext retryContext) {
        retryContext.setRetryStopTimeStamp(System.currentTimeMillis());
        for (RetryListener retryListener : retryListeners) {
            retryListener.onClose(retryContext);
        }
    }

}
