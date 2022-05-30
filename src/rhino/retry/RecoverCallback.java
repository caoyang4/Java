package src.rhino.retry;

/**
 * Created by zhen on 2019/2/21.
 */
public interface RecoverCallback<T> {

    /**
     * 重试失败后的回调
     * @return
     * @throws Throwable
     */
    T recover() throws Throwable;

}
