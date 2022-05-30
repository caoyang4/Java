package src.rhino.onelimiter;

/**
 * Created by zhanjun on 2018/4/20.
 */
public class OneLimiterEntity {
    private String appKey;
    private String rhinoKey;
    private String path;

    public OneLimiterEntity(String appKey, String rhinoKey, String path) {
        this.appKey = appKey;
        this.rhinoKey = rhinoKey;
        this.path = path;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getRhinoKey() {
        return rhinoKey;
    }

    public void setRhinoKey(String rhinoKey) {
        this.rhinoKey = rhinoKey;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}
