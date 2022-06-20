package src.db.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.Test;
import src.juc.JucUtils;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * @author caoyang
 * @create 2022-06-20 23:07
 */
public class TestCaffeine {
    @Test
    public void test1(){
        Cache<String, String> cache = Caffeine.newBuilder().build();
        cache.put("player", "james");
        String player = cache.getIfPresent("player");
        System.out.println(player);
        String defaultPlayer = cache.get("default", key -> {
            return "kobe";
        });
        System.out.println(defaultPlayer);
    }

    @Test
    public void test2() {
        Cache<String, String> cache = Caffeine.newBuilder().maximumSize(1).build();
        cache.put("player", "james");
        cache.put("ball", "basketball");
        cache.put("sex", "male");
        String player = cache.getIfPresent("player");
        String ball = cache.getIfPresent("ball");
        String sex = cache.getIfPresent("sex");
        System.out.println(player);
        System.out.println(ball);
        System.out.println(sex);
        JucUtils.sleepSeconds(1);
        player = cache.getIfPresent("player");
        ball = cache.getIfPresent("ball");
        sex = cache.getIfPresent("sex");
        System.out.println(player);
        System.out.println(ball);
        System.out.println(sex);
    }

    @Test
    public void test3() {
        Cache<String, String> cache = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.SECONDS).build();
        cache.put("player", "james");
        String player = cache.getIfPresent("player");
        System.out.println(player);
        JucUtils.sleepSeconds(1);
        player = cache.getIfPresent("player");
        System.out.println(player);
    }
}
