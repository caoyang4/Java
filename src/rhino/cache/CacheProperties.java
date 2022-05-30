package src.rhino.cache;

import src.rhino.RhinoProperties;

/**
 * @author zhanjun on 2017/09/25.
 */
public interface CacheProperties extends RhinoProperties {

    boolean default_isActive = true;

    int default_connTimeout = 1000;

    int default_readTimeout = 50;

    /**
     * return the component is active or not
     *
     * @return
     */
    boolean getIsActive();

    /**
     * set redis properties
     * @param redisProperties
     */
    void setRedisProperties(RedisProperties redisProperties);

    /**
     * get redis properties
     * @return
     */
    RedisProperties getRedisProperties();
}
