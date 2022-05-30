package src.rhino.Switch;

/**
 * Created by zhanjun on 2018/4/17.
 */
public class SwitchEntity {

    private String appKey;
    private String key;
    private String value;
    private String configName;

    public SwitchEntity(String appKey, String key, String value) {
        this.appKey = appKey;
        this.key = key;
        this.value = value;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }
}
