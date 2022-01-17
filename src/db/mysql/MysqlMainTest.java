package src.db.mysql;

import java.sql.*;

/**
 * @author caoyang
 */
public class MysqlMainTest {
    /**
     * driverUrl=jdbc:mysql://localhost:3306/spring
     * userName=spring
     * password=spring
     */
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/spring";
    static final String USER = "spring";
    static final String PASSWORD = "spring";
    static {
        try {
            Class.forName(JDBC_DRIVER);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {

        try {
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            System.out.println("【1】连接数据库..." + conn);
            // 执行查询
            Statement statement = conn.createStatement();
            System.out.println("【2】创建Statement对象(sql语句)..." + statement);
            String sql = "SELECT * FROM user";
            System.out.println("【3】执行 sql 语句");
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()){
                System.out.println(rs.getString("name") + " " + rs.getString("login_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
