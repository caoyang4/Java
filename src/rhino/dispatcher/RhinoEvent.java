package src.rhino.dispatcher;

/**
 * @author zhanjun on 2017/08/17.
 */
public class RhinoEvent {

    private String preName;
    private RhinoEventType eventType;
    private RhinoEventData eventData;
    private int batchNum = 1;

    public RhinoEvent(RhinoEventType eventType) {
        this.eventType = eventType;
    }

    public RhinoEvent(String preName, RhinoEventType eventType) {
       this.preName = preName;
       this.eventType = eventType;
    }

    public RhinoEvent(RhinoEventType eventType, RhinoEventData eventData) {
        this.eventType = eventType;
        this.eventData = eventData;
    }

    public RhinoEvent(String preName, RhinoEventType eventType, int batchNum) {
        this.preName = preName;
        this.eventType = eventType;
        this.batchNum = batchNum;
    }

    public String getPreName() {
        return preName != null ? preName : "";
    }

    public RhinoEventType getEventType() {
        return eventType;
    }

    public RhinoEventData getEventData() {
        return eventData;
    }

    public int getBatchNum() {
        return batchNum;
    }

    public void setBatchNum(int batchNum) {
        this.batchNum = batchNum;
    }
}
