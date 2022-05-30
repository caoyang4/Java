package src.rhino.fault;

import src.rhino.RhinoType;
import src.rhino.dispatcher.RhinoEventType;

/**
 * @author zhanjun on 2017/08/17.
 */
public enum FaultInjectEventType implements RhinoEventType {

    /**
     * 故障初始化
     */
    INJECT_INITIAL(0, "inject.initial", true),

    /**
     * 故障开始
     */
    INJECT_START(1, "inject.start", true),

    /**
     *故障结束
     */
    INJECT_STOP(2, "inject.stop", true),

    /**
     *故障取消
     */
    INJECT_CANCEL(3, "inject.cancel", true),

    /**
     * 延迟
     */
    DELAY("delay", false),

    /**
     * 异常
     */
    EXCEPTION("exception", false),

    /**
     * MOCK
     */
    MOCK("mock", false),

    /**
     * 参数变更
     */
    CONFIG_CHANGE("configChange", false);



    private RhinoType rhinoType = RhinoType.FaultInject;
    private String type = "Rhino.FaultInject";
    private int index;
    private String value;
    private boolean isNotify;

    FaultInjectEventType(String value, boolean isNotify) {
        this(0, value, isNotify);
    }

    FaultInjectEventType(int index, String value, boolean isNotify) {
        this.index = index;
        this.value = value;
        this.isNotify = isNotify;
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
        return index;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public boolean isNotify() {
        return isNotify;
    }
}
