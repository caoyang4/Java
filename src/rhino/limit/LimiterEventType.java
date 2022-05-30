package src.rhino.limit;

import src.rhino.RhinoType;
import src.rhino.dispatcher.RhinoEventType;

public enum LimiterEventType implements RhinoEventType {

    ACCESS("limit.normal"),

    REFUSE("limit.reject"),

    DEGRADE("limit.degrade"),

    WARN("limit.warn");

    private String value;
    private String type = "Rhino.Limiter";

    LimiterEventType(String value) {
        this.value = value;
    }

    @Override
    public RhinoType getRhinoType() {
        return null;
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
