package src.spring.aop;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author caoyang
 */
public class TestDbQuery {
    public static void main(String[] args) throws SQLException {
        ApplicationContext ctx = new FileSystemXmlApplicationContext("src/spring/jdbc.xml");
        DataSource dataSource = ctx.getBean("dataSource", DataSource.class);
        Connection connection = dataSource.getConnection();
        System.out.println("MySQL connection: " + connection);
        String sql = "SELECT * FROM user";
        // try-with-resource 语句块
        try (Statement statement = connection.createStatement()) {
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()){
                System.out.println(rs.getString("name") + " " + rs.getString("login_name"));
            }
        }
    }
}
