package src.rhino.cache.filter;

/**
 * Created by zhanjun on 2017/09/26.
 */
public class DefaultCacheKeyFilter implements CacheKeyFilter {

    @Override
    public boolean accept(Object key) {
        return true;
    }
}
