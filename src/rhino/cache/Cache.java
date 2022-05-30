package src.rhino.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import src.rhino.RhinoType;
import src.rhino.RhinoUseMode;
import src.rhino.service.RhinoEntity;
import src.rhino.service.RhinoManager;
import src.rhino.util.AppUtils;
import src.rhino.util.ExtensionLoader;

/**
 * @author zhanjun on 2017/09/25.
 */
public interface Cache {

    /**
     * put key, value
     * @param key
     * @param value
     */
    void put(Object key, Object value);

    /**
     * get value by key
     * @param key
     * @return
     */
    Object get(Object key);

    class Factory {

        private static final ConcurrentMap<String, Cache> caches = new ConcurrentHashMap<String, Cache>();
        private static CacheFactory cacheFactory = ExtensionLoader.getExtension(CacheFactory.class);

        public static Cache getInstance(String rhinoKey, CacheProperties properties) {
            return getInstance(rhinoKey, properties, RhinoUseMode.API.getValue());
        }

        /**
         * get or create cache with rhino key and properties
         *
         * @param rhinoKey
         * @param properties
         * @return
         */
        public static Cache getInstance(String rhinoKey, CacheProperties properties, int useMode) {
            if (cacheFactory == null) {
                return null;
            }
            Cache cache = caches.get(rhinoKey);
            if (cache == null) {
                synchronized (Cache.class) {
                    cache = caches.get(rhinoKey);
                    if (cache == null) {
                        cache = new CacheProxy(cacheFactory.create(properties), properties);
                        RhinoManager.report(new RhinoEntity(rhinoKey, RhinoType.Cache, useMode, AppUtils.DEFAULT_CELL, null));
                        caches.put(rhinoKey, cache);
                    }
                }
            }
            return cache;
        }
    }
}
