package src.spring.aop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * @author caoyang
 */
@Component
public class ConnectionFactory {
    @Autowired
    DataSource dataSource;


    public Connection getConnection() throws Exception {
        return dataSource.getConnection();
    }
}
