package src.rhino.cache;

/**
 * @author zhanjun on 2017/09/25.
 */
public interface CacheFactory {

    /**
     * create cache
     * @param properties
     * @return
     */
    Cache create(CacheProperties properties);
}
