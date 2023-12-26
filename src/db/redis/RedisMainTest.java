package src.db.redis;

import com.alibaba.fastjson.JSONObject;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import org.openjdk.jol.info.ClassLayout;
import redis.clients.jedis.Transaction;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public class RedisMainTest {
    final Jedis jedis = new Jedis("127.0.0.1", 6379);

    @Test
    public void testConnection(){
        System.out.println(jedis.ping());
    }

    @Test
    public void testExists(){
        Long exists = jedis.exists("city", "ball", "notExists");
        System.out.println(exists);
    }

    @Test
    public void testKeys(){
        Set<String> keys = jedis.keys("*");
        keys.forEach(System.out::println);
    }

    @Test
    public void testString(){
        jedis.set("client", "jedis");
        jedis.setnx("client", "nx");
        jedis.setnx("clientnx", "nx");
        System.out.println(jedis.get("caoyang"));
        System.out.println(jedis.get("clientnx"));
        System.out.println(jedis.get("client"));
    }

    @Test
    public void testList(){
        List<String> citys = jedis.lrange("city", 0, -1);
        citys.forEach(System.out::println);
        jedis.lpush("city", "paris", "newyork");
        System.out.println(jedis.lpop("city"));
    }

    @Test
    public void testSet(){
        jedis.sadd("fruits", "apple", "pear", "grape");
        jedis.smembers("fruits").forEach(System.out::println);
        jedis.sadd("set1", "a", "b", "c");
        jedis.sadd("set2", "c", "d", "f");
        System.out.println(jedis.sdiff("set1", "set2"));
        System.out.println(jedis.sinter("set1", "set2"));
        System.out.println(jedis.sunion("set1", "set2"));
    }

    @Test
    public void testSortedSet(){
        jedis.zadd("players", 100, "james");
        jedis.zadd("players", 100, "jordan");
        jedis.zadd("players", 60, "paul");
        jedis.zadd("players", 90, "kobe");
        jedis.zadd("players", 80, "kurry");
        jedis.zadd("players", 50, "park");
        System.out.println(jedis.zrangeByScore("players", 0, 100));
    }

    @Test
    public void testHash(){
        jedis.hset("user", "name","wukong");
        jedis.hset("user", "age","10000");
        jedis.hset("user", "nation","China");
        System.out.println(jedis.hmget("user", "name", "age", "nation"));
    }

    @Test
    public void testTransaction() throws IOException {
        JSONObject object = new JSONObject();
        object.put("name", "young");
        object.put("city", "shangahi");
        object.put("age", 18);
        String jsonString = object.toJSONString();
        Transaction transaction = jedis.multi();
        try {
            transaction.set("transaction", jsonString);
            transaction.set("json", jsonString);
            transaction.exec();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println(jedis.get("transaction"));
            System.out.println(jedis.get("json"));
            transaction.close();
        }

    }
}
