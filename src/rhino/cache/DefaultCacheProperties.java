package src.rhino.cache;

import src.rhino.RhinoConfigProperties;
import src.rhino.RhinoType;
import src.rhino.annotation.Cache;
import src.rhino.annotation.JsonIgnore;
import src.rhino.config.ConfigChangedListener;
import src.rhino.config.Configuration;
import src.rhino.util.AppUtils;
import src.rhino.util.PlaceHolderSupport;

/**
 * @author zhanjun on 2017/09/25.
 */
public class DefaultCacheProperties extends RhinoConfigProperties implements CacheProperties {

    private boolean isActive;

    @JsonIgnore
    private PlaceHolderSupport placeHolder = PlaceHolderSupport.getInstance();

    @JsonIgnore
    private RedisProperties redisProperties;

    public DefaultCacheProperties(String rhinoKey) {
        this(AppUtils.getAppName(), rhinoKey, null);
    }

    public DefaultCacheProperties(Cache cache) {
        this(AppUtils.getAppName(), cache.rhinoKey(), cache, null);
    }

    public DefaultCacheProperties(String appKey, String rhinoKey, Configuration config) {
        this(appKey, rhinoKey, null, config);
    }

    public DefaultCacheProperties(String appKey, String rhinoKey, Cache cache, Configuration config) {
        super(appKey, rhinoKey, RhinoType.CircuitBreaker, config);
        boolean isActive = (cache == null) ? default_isActive : cache.isActive();
        this.isActive = getIsActive(isActive);
        String clusterName = placeHolder.getValue((cache == null) ? "" : cache.clusterName());
        String category = placeHolder.getValue((cache == null) ? "" : cache.category());
        int connTimeout = (cache == null) ? default_connTimeout : cache.connTimeout();
        int readTimeout = (cache == null) ? default_readTimeout : cache.readTimeout();
        this.redisProperties = new RedisProperties(clusterName, category, connTimeout, readTimeout);
    }

    private boolean getIsActive(boolean defaultValue) {
        return getBooleanValue("isActive", defaultValue);
    }

    @Override
    public boolean getIsActive() {
        return getIsActive(isActive);
    }

    @Override
    public void setRedisProperties(RedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

    @Override
    public RedisProperties getRedisProperties() {
        return this.redisProperties;
    }

    @Override
    public void addConfigChangedListener(ConfigChangedListener listener) {
        //Do nothing
    }
}
