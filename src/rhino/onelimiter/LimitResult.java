package src.rhino.onelimiter;

import java.util.List;

/**
 * Created by zhanjun on 2018/4/13.
 */
public class LimitResult {

    /**
     * 默认是pass状态
     */
    private Status status = Status.PASS;

    /**
     * 生效的限流策略
     */
    private OneLimiterStrategyEnum strategyEnum;

    /**
     * 触发预警的策略
     */
    private List<OneLimiterStrategyEnum> warnStrategies;

    /**
     * 拒绝时返回的代号，默认返回200
     */
    private int code = 200;

    /**
     * 拒绝时返回的消息
     */
    private String msg;

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public OneLimiterStrategyEnum getStrategyEnum() {
        return strategyEnum;
    }

    public boolean isPass() {
        return status.isPass();
    }

    public boolean isReject() {
        return status.isReject();
    }

    protected void setReject() {
        this.status = Status.REJECT;
    }

    protected void setCode(int code) {
        this.code = code;
    }

    protected void setMsg(String msg) {
        this.msg = msg;
    }

    protected void setStrategyEnum(OneLimiterStrategyEnum strategyEnum) {
        this.strategyEnum = strategyEnum;
    }

    public List<OneLimiterStrategyEnum> getWarnStrategies() {
        return warnStrategies;
    }

    public void setWarnStrategies(List<OneLimiterStrategyEnum> warnStrategies) {
        this.warnStrategies = warnStrategies;
    }

    @Override
    public String toString() {
        return "{\"strategy\": \"" + strategyEnum + "\", \"status\": \"" + status + "\", \"code\": " + code + ", \"msg\": \"" + msg + "\"}";
    }

    enum Status {

        /**
         * 拒绝
         */
        REJECT,

        /**
         * 通过
         */
        PASS;

        /**
         * @return
         */
        public boolean isReject() {
            return REJECT.equals(this);
        }

        /**
         * @return
         */
        public boolean isPass() {
            return PASS.equals(this);
        }
    }
}
