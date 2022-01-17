package src.designPattern.observer;

import java.sql.Timestamp;

/**
 * 事件信息
 * @author caoyang
 */
public class WakeUpEvent {
    /**
     * 时间戳
     */
    private Long timestamp;
    /**
     * 事件发生地
     */
    private String location;
    /**
     * 事件主体
     */
    private Child child;


    public WakeUpEvent(Long timestamp, String location, Child child) {
        this.timestamp = timestamp;
        this.location = location;
        this.child = child;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getLocation() {
        return location;
    }

    public Child getChild() {
        return child;
    }
}
