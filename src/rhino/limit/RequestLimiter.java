package src.rhino.limit;

import java.util.concurrent.ConcurrentHashMap;

import com.mysql.cj.util.StringUtils;

import src.rhino.RhinoType;
import src.rhino.RhinoUseMode;
import src.rhino.cache.RedisProperties;
import src.rhino.limit.feature.Features;
import src.rhino.log.Logger;
import src.rhino.log.LoggerFactory;
import src.rhino.service.RhinoEntity;
import src.rhino.service.RhinoManager;
import src.rhino.util.AppUtils;
import src.rhino.util.CommonUtils;

/**
 * @author zhanjun on 2017/4/21.
 */
public interface RequestLimiter {

    /**
     * if get access, return true
     *
     * @return
     */
    boolean tryAcquire();

    /**
     * get access for key
     *
     * @param key
     * @return
     */
    boolean tryAcquire(String key);

    /**
     * get access for features
     *
     * @param features
     * @return
     */
    boolean tryAcquire(Features features);


    /**
     * get access for features with permits
     *
     * @param features
     * @param permits
     * @return
     */
    boolean tryAcquire(Features features, int permits);

    /**
     * is need to degrade
     *
     * @return
     */
    boolean isNeedDegrade();

    /**
     * set redis properties
     *
     * @param redisProperties
     */
    void setRedisProperties(RedisProperties redisProperties);

    class Factory {

        private static final Logger logger = LoggerFactory.getLogger(Factory.class);
        private static final RequestLimiter EMPTY = new NoOpRequestLimiter();
        private static ConcurrentHashMap<String, RequestLimiter> requestLimiters = new ConcurrentHashMap<>();

        public static RequestLimiter getInstance(String rhinoKey, RequestLimiterProperties requestLimiterProperties) {
            return getInstance(rhinoKey, requestLimiterProperties, RhinoUseMode.API.getValue());
        }

        /**
         * 初始化单个限流器实例
         *
         * @param rhinoKey
         * @param requestLimiterProperties
         * @return
         */
        public static RequestLimiter getInstance(String rhinoKey, RequestLimiterProperties requestLimiterProperties, int useMode) {
            if (StringUtils.isNullOrEmpty(rhinoKey)) {
                return EMPTY;
            }
            rhinoKey += AppUtils.getSetSuffix();
            RequestLimiter requestLimiter = requestLimiters.get(rhinoKey);
            if (requestLimiter == null) {
                synchronized (RequestLimiter.class) {
                    requestLimiter = requestLimiters.get(rhinoKey);
                    if (requestLimiter == null) {
                        if (requestLimiterProperties == null) {
                            requestLimiterProperties = new DefaultRequestLimiterProperties(rhinoKey);
                        }
                        requestLimiter = new SingleRequestLimiter(rhinoKey, requestLimiterProperties);
                        RhinoManager.report(new RhinoEntity(rhinoKey, RhinoType.SingleLimiter, useMode, AppUtils.getSet(), CommonUtils.parseProperties((DefaultRequestLimiterProperties) requestLimiterProperties)));
                        requestLimiters.put(rhinoKey, requestLimiter);
                        requestLimiterProperties.addConfigChangedListener(null);
                    }
                }
            }
            return requestLimiter;
        }

        /**
         * 初始化一个限流器组
         *
         * @param rhinoKey
         * @param requestLimiterProperties
         * @return
         */
        public static RequestLimiter getGroupInstance(String rhinoKey, RequestLimiterProperties requestLimiterProperties) {
            if (StringUtils.isNullOrEmpty(rhinoKey)) {
                return EMPTY;
            }
            rhinoKey += AppUtils.getSetSuffix();
            RequestLimiter requestLimiter = requestLimiters.get(rhinoKey);
            if (requestLimiter == null) {
                synchronized (RequestLimiter.class) {
                    requestLimiter = requestLimiters.get(rhinoKey);
                    if (requestLimiter == null) {
                        if (requestLimiterProperties == null) {
                            requestLimiterProperties = new DefaultRequestLimiterProperties(rhinoKey);
                        }
                        requestLimiter = new GroupRequestLimiter(rhinoKey, requestLimiterProperties);
                        RhinoManager.report(new RhinoEntity(rhinoKey, RhinoType.GroupLimiter, RhinoUseMode.API.getValue(), AppUtils.getSet(), CommonUtils.parseProperties((DefaultRequestLimiterProperties) requestLimiterProperties)));
                        requestLimiters.put(rhinoKey, requestLimiter);
                        requestLimiterProperties.addConfigChangedListener(null);
                    }
                }
            }
            return requestLimiter;
        }

        /**
         * 初始化一个限流器组
         *
         * @param rhinoKey
         * @param requestLimiterProperties
         * @return
         */
        public static RequestLimiter getClusterInstance(String rhinoKey, RequestLimiterProperties requestLimiterProperties) {
            RequestLimiter requestLimiter = requestLimiters.get(rhinoKey);
            if (requestLimiter == null) {
                synchronized (RequestLimiter.class) {
                    requestLimiter = requestLimiters.get(rhinoKey);
                    if (requestLimiter == null) {
                        if (requestLimiterProperties == null) {
                            requestLimiterProperties = new DefaultRequestLimiterProperties(rhinoKey);
                        }
                        requestLimiter = new ClusterRequestLimiter(rhinoKey, requestLimiterProperties);
                        RhinoManager.report(new RhinoEntity(rhinoKey, RhinoType.ClusterLimiter, RhinoUseMode.API.getValue(), AppUtils.getSet(), CommonUtils.parseProperties((DefaultRequestLimiterProperties) requestLimiterProperties)));
                        requestLimiters.put(rhinoKey, requestLimiter);
                        requestLimiterProperties.addConfigChangedListener(null);
                    }
                }
            }
            return requestLimiter;
        }

        /**
         * 初始化一个多维度限流器
         *
         * @param rhinoKey
         * @param requestLimiterProperties
         * @return
         */
        public static RequestLimiter getFeatureInstance(String rhinoKey, RequestLimiterProperties requestLimiterProperties) {
            if (StringUtils.isNullOrEmpty(rhinoKey)) {
                return EMPTY;
            }
            rhinoKey += AppUtils.getSetSuffix();
            RequestLimiter requestLimiter = requestLimiters.get(rhinoKey);
            if (requestLimiter == null) {
                synchronized (RequestLimiter.class) {
                    requestLimiter = requestLimiters.get(rhinoKey);
                    if (requestLimiter == null) {
                        if (requestLimiterProperties == null) {
                            requestLimiterProperties = new FeatureRequestLimiterProperties(rhinoKey);
                        }
                        requestLimiter = new FeatureRequestLimiter(rhinoKey, requestLimiterProperties);
                        RhinoManager.report(new RhinoEntity(rhinoKey, RhinoType.FeatureLimiter, RhinoUseMode.API.getValue(), AppUtils.getSet(), CommonUtils.parseProperties((FeatureRequestLimiterProperties) requestLimiterProperties)));
                        requestLimiters.put(rhinoKey, requestLimiter);
                        requestLimiterProperties.addConfigChangedListener(null);
                    }
                }
            }
            return requestLimiter;
        }


        public static RequestLimiter getEmpty() {
            return EMPTY;
        }
    }
}
