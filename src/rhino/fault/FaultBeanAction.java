package src.rhino.fault;

/**
 * @author zhanjun
 * @date 2017/12/07
 */
public enum FaultBeanAction {

    INITIAL(FaultInjectEventType.INJECT_INITIAL),

    CANCEL(FaultInjectEventType.INJECT_CANCEL),

    START(FaultInjectEventType.INJECT_START),

    STOP(FaultInjectEventType.INJECT_STOP);

    private FaultInjectEventType eventType;

    FaultBeanAction(FaultInjectEventType eventType) {
        this.eventType = eventType;
    }

    public FaultInjectEventType getEventType() {
        return eventType;
    }
}
