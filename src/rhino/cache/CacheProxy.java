package src.rhino.cache;

import src.rhino.Rhino;
import src.rhino.cache.filter.CacheKeyFilter;
import src.rhino.threadpool.ThreadPool;
import src.rhino.util.ExtensionLoader;

/**
 * @author  zhanjun on 2017/09/25.
 */
public class CacheProxy implements Cache {

    private static ThreadPool threadPool = Rhino.newThreadPool("Rhino-Cache-ThreadPool");
    private static CacheKeyFilter cacheKeyFilter = ExtensionLoader.newExtension(CacheKeyFilter.class);
    private Cache cache;
    private CacheProperties properties;

    public CacheProxy(Cache cache, CacheProperties properties) {
        this.cache = cache;
        this.properties = properties;
    }

    /**
     * 判断是否开启
     * @return
     */
    private boolean isActive() {
        return properties.getIsActive();
    }

    /**
     * 异步写入缓存
     * @param key
     * @param value
     */
    @Override
    public void put(Object key, Object value) {
        if (isActive() && key != null) {
            threadPool.execute(new CacheOperationTask(cache, key, value));
        }
    }

    /**
     * 同步读取缓存
     * @param key
     * @return
     */
    @Override
    public Object get(Object key) {
        if (isActive() && key != null && cacheKeyFilter.accept(key)) {
            return cache.get(key);
        }
        return null;
    }

    /**
     * 缓存写入任务
     */
    static class CacheOperationTask implements Runnable {
        private Cache cache;
        private Object key;
        private Object value;

        public CacheOperationTask(Cache cache, Object key, Object value) {
            this.cache = cache;
            this.key = key;
            this.value = value;
        }

        @Override
        public void run() {
            if (cacheKeyFilter.accept(key.toString())) {
                cache.put(key, value);
            }
        }
    }
}
