package src.db.ehcache;

import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * Encache测试
 */
@Slf4j(topic = "TestEncache")
public class TestEhcache {
    public static void main(String[] args) {
        CacheManager cacheManager = CacheManager.create("/Users/caoyang/IdeaProjects/Java/src/db/ehcache/cache.xml");
        Cache cache = cacheManager.getCache("YoungCache");

        Element name = new Element("name", "young");
        Element age = new Element("age", 28);
        cache.put(name);
        cache.put(age);
        log.info("{}: {}",cache.getName(), cache.getKeys());
        log.info("name: {}", cache.get(name));
        log.info("age: {}", cache.get(age));
    }
}
