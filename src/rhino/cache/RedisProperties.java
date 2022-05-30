package src.rhino.cache;

/**
 * @author zhanjun on 2017/10/16.
 */
public class RedisProperties {

    private String clusterName;
    private String category;
    private int readTimeout = 50;
    private int connTimeout = 1000;

    public RedisProperties(String clusterName) {
        this.clusterName = clusterName;
    }

    public RedisProperties(String clusterName, String category) {
        this.clusterName = clusterName;
        this.category = category;
    }

    public RedisProperties(String clusterName, String category, int readTimeout, int connTimeout) {
        this.clusterName = clusterName;
        this.category = category;
        this.readTimeout = readTimeout;
        this.connTimeout = connTimeout;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getConnTimeout() {
        return connTimeout;
    }

    public void setConnTimeout(int connTimeout) {
        this.connTimeout = connTimeout;
    }
}
