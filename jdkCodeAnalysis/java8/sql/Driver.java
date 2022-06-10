package java.sql;

import java.util.logging.Logger;

public interface Driver {
    // 尝试建立给定URL的数据库连接
    Connection connect(String url, java.util.Properties info) throws SQLException;
    // 检索驱动程序是可以打开与给定URL的连接
    boolean acceptsURL(String url) throws SQLException;
    // 获取有关此驱动程序可能的属性的信息
    DriverPropertyInfo[] getPropertyInfo(String url, java.util.Properties info) throws SQLException;

    int getMajorVersion();

    int getMinorVersion();

    // 此驱动程序是否是正版JDBC Compliant™驱动程序
    boolean jdbcCompliant();


    public Logger getParentLogger() throws SQLFeatureNotSupportedException;
}
