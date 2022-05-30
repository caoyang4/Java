package src.rhino.retry;

import java.util.concurrent.ConcurrentHashMap;

import com.mysql.cj.util.StringUtils;

import src.rhino.RhinoType;
import src.rhino.RhinoUseMode;
import src.rhino.retry.listener.RetryListener;
import src.rhino.service.RhinoEntity;
import src.rhino.service.RhinoManager;
import src.rhino.util.AppUtils;
import src.rhino.util.CommonUtils;

/**
 * Created by zhen on 2019/2/22.
 */
public interface Retry {

    /**
     *
     * @param retryCallback 业务逻辑的回调
     * @param <T> 返回值
     * @param <E> 可能抛出的异常
     * @return
     * @throws E
     */
    <T, E extends Throwable> T execute(RetryCallback<T, E> retryCallback) throws E;

    /**
     * 需要透传业务自定义参数的执行入口
     * @param retryCallback 业务逻辑的回调
     * @param recoverCallback 重试失败后业务降级的回调
     * @param args 业务自定义参数
     * @param <T> 返回值
     * @param <E> 可能抛出的异常
     * @return
     * @throws E
     */
    <T, E extends Throwable> T execute(RetryCallback<T, E> retryCallback, RecoverCallback<T> recoverCallback, Object... args) throws E;

    /**
     *
     * @param retryListener
     * @return
     */
    boolean addRetryListener(RetryListener retryListener);

    boolean canRetry(RetryContext retryContext);

    class Factory {

        private static Retry EMPTY = NoOpRetry.instance;
        private static ConcurrentHashMap<String, Retry> retryHolder = new ConcurrentHashMap<>();

        public static Retry getInstance(String rhinoKey) {
            return getInstance(rhinoKey, null, RhinoUseMode.API);
        }

        public static Retry getInstance(String rhinoKey, DefaultRetryProperties.Setter setter) {
            return getInstance(rhinoKey, setter, RhinoUseMode.API);
        }

        public static Retry getInstance(String rhinoKey, DefaultRetryProperties.Setter setter, RhinoUseMode mode) {
            if (StringUtils.isNullOrEmpty(rhinoKey)) {
                return EMPTY;
            }
            rhinoKey += AppUtils.getSetSuffix();
            Retry retry = retryHolder.get(rhinoKey);
            if (retry == null) {
                synchronized (Retry.class) {
                    retry = retryHolder.get(rhinoKey);
                    if (retry == null) {
                        DefaultRetryProperties retryProperties = new DefaultRetryProperties(rhinoKey, setter);
                        retry = new DefaultRetry(retryProperties);
                        retryHolder.put(rhinoKey, retry);
                        RhinoManager.report(new RhinoEntity(rhinoKey, RhinoType.RetryPolicy, mode.getValue(), AppUtils.getSet(), CommonUtils.parseProperties(retryProperties)));
                    }
                }
            }
            return retry;
        }

        public static Retry getEMPTY() {
            return EMPTY;
        }
    }

}
