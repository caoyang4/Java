package src.rhino.onelimiter;

/**
 * Created by zhanjun on 2018/4/14.
 */
public enum ExecuteStatus {
    /**
     * 直接通过（白名单）
     */
    DIRECT_PASS,

    /**
     * 拒绝（黑名单、限流）
     */
    DIRECT_REJECT,

    /**
     * 通过，继续下一个策略
     */
    CONTINUE,

    /**
     * 超过80%预警线
     */
    CONTINUE_AND_WARN;

    public boolean isTerminal() {
        return DIRECT_PASS.equals(this) || DIRECT_REJECT.equals(this);
    }

    public boolean isWarn() {
        return CONTINUE_AND_WARN.equals(this);
    }

    public boolean isReject() {
        return DIRECT_REJECT.equals(this);
    }
}
