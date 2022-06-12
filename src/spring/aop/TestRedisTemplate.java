package src.spring.aop;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author caoyang
 * @create 2022-06-11 23:29
 */
public class TestRedisTemplate {
    final ApplicationContext ctx = new FileSystemXmlApplicationContext("src/spring/spring-redis.xml");
    final RedisTemplate redisTemplate = (RedisTemplate) ctx.getBean("redisTemplate");

    @Test
    public void test1(){
        System.out.println(redisTemplate.hasKey("city"));
        System.out.println(redisTemplate.keys("*"));
    }

    @Test
    public void test2(){
        redisTemplate.opsForValue().set("templateString", "redisTemplate");
        System.out.println(redisTemplate.opsForValue().get("templateString"));
    }

    @Test
    public void test3(){
        redisTemplate.opsForSet().add("templateSet", "ele1", "ele2", "ele3");
        System.out.println(redisTemplate.opsForSet().members("templateSet"));
    }

    @Test
    public void test4(){
        if (redisTemplate.hasKey("templateList"))
            redisTemplate.delete("templateList");

        redisTemplate.opsForList().leftPush("templateList", "ele1");
        redisTemplate.opsForList().leftPush("templateList", "ele2");
        redisTemplate.opsForList().rightPush("templateList", "ele3");
        redisTemplate.opsForList().range("templateList", 0, -1).forEach(System.out::println);
    }

    @Test
    public void test5() {
        if (redisTemplate.hasKey("templateHash"))
            redisTemplate.delete("templateHash");

        redisTemplate.opsForHash().put("templateHash", "key1", "value1");
        redisTemplate.opsForHash().put("templateHash", "key2", "value2");
        redisTemplate.opsForHash().put("templateHash", "key3", "value3");
        System.out.println(redisTemplate.opsForHash().get("templateHash", "key1"));
        System.out.println(redisTemplate.opsForHash().entries("templateHash"));
    }


}
