package src.rhino.threadpool.alarm;

/**
 * 线程池告警数据
 * Created by zmz on 2020/11/12.
 */
public class ThreadPoolAlarmData {
    private String appKey;
    private String rhinoKey;
    private int level;
    private long timestamp;
    private String detail;
    private int triggers;  //不同的bit，表示是否触发了对应类型的告警

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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public int getTriggers() {
        return triggers;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setTriggers(int triggers) {
        this.triggers = triggers;
    }
}
