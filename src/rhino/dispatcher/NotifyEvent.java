package src.rhino.dispatcher;

import src.rhino.RhinoType;

/**
 * Created by zhanjun on 2017/7/7.
 */
public class NotifyEvent {

    private RhinoType rhinoType;
    private String rhinoKey;
    private RhinoEventType eventType;
    private String data;
    private long eventTime = System.currentTimeMillis();

    public NotifyEvent(RhinoType rhinoType, String rhinoKey, RhinoEventType eventType, String data) {
        this.rhinoType = rhinoType;
        this.rhinoKey = rhinoKey;
        this.eventType = eventType;
        this.data = data;
    }

    public RhinoType getRhinoType() {
        return rhinoType;
    }

    public String getRhinoKey() {
        return rhinoKey;
    }

    public RhinoEventType getEventType() {
        return eventType;
    }

    public long getEventTime() {
        return eventTime;
    }

    public String getData() {
        return data;
    }
}
