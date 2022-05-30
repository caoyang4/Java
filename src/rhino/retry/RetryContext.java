package src.rhino.retry;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zhen on 2019/2/20.
 */
public class RetryContext {

    private long retryStartTimestamp = System.currentTimeMillis();
    private long retryStopTimeStamp;
    private int attempt = 0;
    private Throwable lastThrowable;
    private State state = State.Not_Started;
    private Map<Class<? extends Throwable>, Integer> exceptionSummary = new ConcurrentHashMap<>();
    private Object result;
    private List<Long> delayHistory = new LinkedList<>();
    private Object[] args;

    public RetryContext(){
    }

    public RetryContext(Object[] args){
        this.args = args;
    }

    /**
     * 记录异常
     *
     * @param e
     */
    void recordThrowable(Throwable e) {
        state = State.Running;
        lastThrowable = e;
        Class<? extends Throwable> eClass = e.getClass();
        if (!exceptionSummary.containsKey(eClass)) {
            exceptionSummary.put(eClass, 0);
        }
        exceptionSummary.put(eClass, exceptionSummary.get(eClass) + 1);
        attempt++;
    }


    void recordResultSuccess(Object result) {
        this.result = result;
        state = State.Close_Success;
    }

    void recordInterrupt() {
        state = State.Close_Interrupted;
    }

    void recordResultError() {
        state = State.Close_Error;
    }

    void recordRecover(Object result) {
        this.result = result;
        state = State.Close_Recover;
    }

    public int getAttempt() {
        return attempt;
    }

    public Throwable getLastThrowable() {
        return lastThrowable;
    }

    public long getRetryStartTimestamp() {
        return retryStartTimestamp;
    }

    public void setRetryStartTimestamp(long retryStartTimestamp) {
        this.retryStartTimestamp = retryStartTimestamp;
    }

    public long getRetryStopTimeStamp() {
        return retryStopTimeStamp;
    }

    public void setRetryStopTimeStamp(long retryStopTimeStamp) {
        this.retryStopTimeStamp = retryStopTimeStamp;
    }

    public void setAttempt(int attempt) {
        this.attempt = attempt;
    }

    public void setLastThrowable(Throwable lastThrowable) {
        this.lastThrowable = lastThrowable;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Map<Class<? extends Throwable>, Integer> getExceptionSummary() {
        return exceptionSummary;
    }

    public void setExceptionSummary(Map<Class<? extends Throwable>, Integer> exceptionSummary) {
        this.exceptionSummary = exceptionSummary;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public List<Long> getDelayHistory() {
        return delayHistory;
    }

    public void setDelayHistory(List<Long> delayHistory) {
        this.delayHistory = delayHistory;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public void registerSleepDelay(long sleepDelay) {
        delayHistory.add(sleepDelay);
    }

    @Override
    public String toString() {
        return "RetryContext{" +
                "retryStartTimestamp=" + retryStartTimestamp +
                ", retryStopTimeStamp=" + retryStopTimeStamp +
                ", attempt=" + attempt +
                ", lastThrowable=" + lastThrowable +
                ", state=" + state +
                ", exceptionSummary=" + exceptionSummary +
                ", result=" + result +
                ", delayHistory=" + delayHistory +
                '}';
    }

    enum State {
        /**
         * 初始状态
         */
        Not_Started(RetryEventType.RETRY_OPEN),
        /**
         * 正在retry 状态
         */
        Running(RetryEventType.RETRY_RUNNING),
        Close_Success(RetryEventType.RETRY_CLOSE_SUCCESS),
        Close_Error(RetryEventType.RETRY_CLOSE_ERROR),
        Close_Recover(RetryEventType.RETRY_CLOSE_RECOVER),
        Close_Interrupted(RetryEventType.RETRY_CLOSE_INTERRUPTED);

        private RetryEventType retryEventType;

        State(RetryEventType retryEventType) {
            this.retryEventType = retryEventType;
        }

        public RetryEventType getRetryEventType() {
            return retryEventType;
        }

        public void setRetryEventType(RetryEventType retryEventType) {
            this.retryEventType = retryEventType;
        }
    }
}
