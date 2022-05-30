package src.rhino.retry;

import src.rhino.RhinoType;
import src.rhino.dispatcher.RhinoEventType;

/**
 * Created by zhen on 2019/2/25.
 */
public enum RetryEventType implements RhinoEventType {

    /**
     * 进入retry
     */
    RETRY_OPEN("retry.open", false),
    /**
     * retry 进行
     */
    RETRY_RUNNING("retry.running", false),
    /**
     * retry结束，返回正确结果
     */
    RETRY_CLOSE_SUCCESS("retry.close.success", false),
    /**
     * retry结束，返回用户定义恢复结果
     */
    RETRY_CLOSE_RECOVER("retry.close.recover", false),
    /**
     * retry结束，跑出异常
     */
    RETRY_CLOSE_ERROR("retry.close.error", false),
    /**
     * retry结束，被interrupt
     */
    RETRY_CLOSE_INTERRUPTED("retry.close.interrupted", false)
    ;

    RetryEventType(String value, boolean notify) {
        this.value = value;
        this.notify = notify;
    }

    private String value;
    private boolean notify;

    @Override
    public RhinoType getRhinoType() {
        return RhinoType.RetryPolicy;
    }

    @Override
    public String getType() {
        return "Rhino.Retry";
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
        return notify;
    }
}
