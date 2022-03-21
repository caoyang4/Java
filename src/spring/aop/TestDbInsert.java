package src.spring.aop;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

/**
 * 编程式事务
 * @author caoyang
 */
public class TestDbInsert {
    public static void main(String[] args) {
        ApplicationContext ctx = new FileSystemXmlApplicationContext("src/spring/jdbc.xml");
        DataSource dataSource = ctx.getBean("dataSource", DataSource.class);
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        //事务状态类，通过PlatformTransactionManager的getTransaction方法根据事务定义获取；获取事务状态后，Spring根据传播行为来决定如何开启事务
        PlatformTransactionManager txManager = new DataSourceTransactionManager(dataSource);
        TransactionStatus status = txManager.getTransaction(def);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        final String insert = "insert into user(name, login_name, credit_id) values('james', 'king', '23')";
        try {
            jdbcTemplate.execute(insert);
            int exp = 1 / 0;
            txManager.commit(status);
        } catch (Exception e){
            e.printStackTrace();
            txManager.rollback(status);
        }

    }
}
