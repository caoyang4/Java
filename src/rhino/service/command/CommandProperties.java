package src.rhino.service.command;

import java.util.Map;

/**
 * Created by zhen on 2018/11/29.
 */
public class CommandProperties {

    private String rhinoKey;
    private int rhinoType;
    private int operationType;
    private Map<String, String> params;

    public String getRhinoKey() {
        return rhinoKey;
    }

    public void setRhinoKey(String rhinoKey) {
        this.rhinoKey = rhinoKey;
    }

    public int getRhinoType() {
        return rhinoType;
    }

    public void setRhinoType(int rhinoType) {
        this.rhinoType = rhinoType;
    }

    public int getOperationType() {
        return operationType;
    }

    public void setOperationType(int operationType) {
        this.operationType = operationType;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }
}
