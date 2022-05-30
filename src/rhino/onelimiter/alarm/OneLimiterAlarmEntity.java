package src.rhino.onelimiter.alarm;

import java.util.List;

/**
 * Created by zhanjun on 2018/4/26.
 */
public class OneLimiterAlarmEntity {
    private String appKey;
    private String rhinoKey;
    private String strategy;
    private String entrance;
    private String params;
    private String type;
    private long count;
    private List<OneLimiterQpsEntity> qpsEntityList;

    public OneLimiterAlarmEntity() {
    }

    public OneLimiterAlarmEntity(String appKey, String rhinoKey) {
        this.appKey = appKey;
        this.rhinoKey = rhinoKey;
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

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public String getEntrance() {
        return entrance;
    }

    public void setEntrance(String entrance) {
        this.entrance = entrance;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<OneLimiterQpsEntity> getQpsEntityList() {
        return qpsEntityList;
    }

    public void setQpsEntityList(List<OneLimiterQpsEntity> qpsEntityList) {
        this.qpsEntityList = qpsEntityList;
    }


}
