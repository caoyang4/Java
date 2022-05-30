package src.rhino.cache.filter;

/**
 * Created by zhanjun on 2017/09/26.
 */
public interface CacheKeyFilter {

    boolean accept(Object key);

}
