package src.spring.aop;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.util.Collection;

/**
 * @author caoyang
 */
public class TestAop {
    public static void main(String[] args) {
        ApplicationContext ctx = new FileSystemXmlApplicationContext("src/spring/jdbc.xml");
        ConnectionFactory connectionFactory = (ConnectionFactory) ctx.getBean("connectionFactory");
    }
}
