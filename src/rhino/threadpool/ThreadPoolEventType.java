package src.rhino.threadpool;

import src.rhino.RhinoType;
import src.rhino.dispatcher.RhinoEventType;

/**
 * @author zhanjun
 */
public enum ThreadPoolEventType implements RhinoEventType {

    REJECT("reject.count");

    private String value;
    private String type = "Rhino.ThreadPool";
    private RhinoType rhinoType = RhinoType.ThreadPool;

    ThreadPoolEventType(String value) {
        this.value = value;
    }

    @Override
    public RhinoType getRhinoType() {
        return rhinoType;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public int getIndex() {
        return 0;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public boolean isNotify() {
        return false;
    }
}
