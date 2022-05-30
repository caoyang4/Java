package src.rhino.service;

import src.rhino.RhinoType;
import src.rhino.config.ConfigFactory;
import src.rhino.util.AppUtils;

/**
 * @author zhanjun on 2017/5/24.
 */
public class RhinoEntity {

    private String appKey = AppUtils.getAppName();
    private String configName = ConfigFactory.getInstance().getName();
    private String rhinoKey;
    private RhinoType type;
    private int useMode;
    private String set;
    private String properties;

    public RhinoEntity(String rhinoKey, RhinoType type, int useMode, String set, String properties) {
        this.rhinoKey = rhinoKey;
        this.type = type;
        this.useMode = useMode;
        this.set = set;
        this.properties = properties;
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

    public RhinoType getType() {
        return type;
    }

    public void setType(RhinoType type) {
        this.type = type;
    }

    public String getProperties() {
        return properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    public int getUseMode() {
        return useMode;
    }

    public void setUseMode(int useMode) {
        this.useMode = useMode;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public String getSet() {
        return set;
    }

    public void setSet(String set) {
        this.set = set;
    }
}
