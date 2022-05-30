package src.rhino.onelimiter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import src.rhino.RhinoType;
import src.rhino.RhinoUseMode;
import src.rhino.service.RhinoEntity;
import src.rhino.service.RhinoManager;
import src.rhino.util.AppUtils;

/**
 * Created by zhanjun on 2018/4/14.
 */
public interface OneLimiter {

    /**
     * 默认限流入口
     * @param entrance 资源标识
     * @return
     */
    LimitResult run(String entrance);

    /**
     * 指定参数匹配的限流入口
     * @param entrance 资源标识
     * @param reqParams 自定义匹配参数
     * @return
     */
    LimitResult run(String entrance, Map<String, String> reqParams);

    /**
     * 指定参数和所需令牌数的限流入口
     * @param entrance 资源标识
     * @param reqParams 自定义匹配参数
     * @param tokens 请求所需令牌数
     * @return
     */
    LimitResult run(String entrance, Map<String, String> reqParams, int tokens);

    /**
     * @return
     */
    OneLimiterManager getOneLimiterManager();

    class Factory {
        private static ConcurrentHashMap<String, OneLimiter> oneLimiters = new ConcurrentHashMap<>();

        public static OneLimiter getInstance(String rhinoKey) {
            OneLimiter flowLimiter = oneLimiters.get(rhinoKey);
            if (flowLimiter == null) {
                synchronized (OneLimiter.class) {
                    flowLimiter = oneLimiters.get(rhinoKey);
                    if (flowLimiter == null) {
                        flowLimiter = new DefaultOneLimiter(rhinoKey);
                        RhinoEntity rhinoEntity = new RhinoEntity(rhinoKey, RhinoType.OneLimiter, RhinoUseMode.API.getValue(), AppUtils.DEFAULT_CELL, null);
                        rhinoEntity.setConfigName(OneLimiterManager.getConfig().getName());
                        RhinoManager.report(rhinoEntity);
                        oneLimiters.put(rhinoKey, flowLimiter);
                    }
                }
            }
            return flowLimiter;
        }

        public static OneLimiter getOneLimiter(String rhinoKey) {
            return oneLimiters.get(rhinoKey);
        }
    }
}
