package src.rhino.retry;

/**
 * Created by zhen on 2019/2/21.
 */
public interface RetryCallback<T,E extends Throwable> {

    /**
     * 业务逻辑
     * @return
     * @throws E
     */
    T doWithRetry() throws E;

}
