package src.db.redis;

import redis.clients.jedis.Jedis;
import org.openjdk.jol.info.ClassLayout;

public class RedisMainTest {
    public static void main(String[] args) {
        Jedis jedis = new Jedis("127.0.0.1", 6379);
        System.out.println(jedis.ping());
        String name = jedis.get("caoyang");
        System.out.println(name);

    }
}
