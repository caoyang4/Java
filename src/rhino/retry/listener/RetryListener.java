package src.rhino.retry.listener;

import src.rhino.retry.RetryContext;

/**
 * Created by zhen on 2019/2/22.
 */
public interface RetryListener {

    /**
     * retry开启的回调
     * 允许用户对 retryContext 加工
     * @param retryContext
     */
    void onOpen(RetryContext retryContext);

    /**
     * retry结束的回调
     * retryContext 包含此次retry的所有信息
     * @param retryContext
     */
    void onClose(RetryContext retryContext);

    /**
     * 用户业务抛出异常的回调
     * @param retryContext
     */
    void onError(RetryContext retryContext, Throwable throwable);
}
