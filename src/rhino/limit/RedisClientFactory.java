package src.rhino.limit;

import src.rhino.cache.RedisProperties;

/**
 * @author zhanjun
 * @date 2017/10/16
 */
public interface RedisClientFactory<T> {

    /**
     * create redis client
     * @param redisProperties
     * @return
     */
    T create(RedisProperties redisProperties);

    /**
     * create redis client
     * @return
     */
    T create(String clusterName);

    /**
     * create redis client
     * @return
     */
    T create(String clusterName, boolean useSetMode);

}
