package src.spring.aop;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * @author caoyang
 * @create 2022-06-11 23:29
 */
public class TestRedisTemplate {
    public static void main(String[] args) {
        ApplicationContext ctx = new FileSystemXmlApplicationContext("src/spring/spring-redis.xml");
//        ctx.getBean("redisUtil");
    }
}
