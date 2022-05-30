package src.rhino.retry;

import src.rhino.retry.listener.RetryListener;

/**
 * Created by zhen on 2019/3/12.
 */
public class NoOpRetry implements Retry {

    public static final Retry instance = new NoOpRetry();

    private NoOpRetry() {
    }

    @Override
    public <T, E extends Throwable> T execute(RetryCallback<T, E> retryCallback) throws E {
        return retryCallback.doWithRetry();
    }

    @Override
    public <T, E extends Throwable> T execute(RetryCallback<T, E> retryCallback, RecoverCallback<T> recoverCallback, Object... args) throws E {
        return retryCallback.doWithRetry();
    }

    @Override
    public boolean addRetryListener(RetryListener retryListener) {
        return false;
    }

    @Override
    public boolean canRetry(RetryContext retryContext) {
        return false;
    }
}
