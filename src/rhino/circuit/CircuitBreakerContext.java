package src.rhino.circuit;

import static src.rhino.circuit.CircuitBreakerContext.ResultState.COMPLETE;
import static src.rhino.circuit.CircuitBreakerContext.ResultState.FAILED;
import static src.rhino.circuit.CircuitBreakerContext.ResultState.NOT_STARTED;
import static src.rhino.circuit.CircuitBreakerContext.ResultState.SUCCESS;

import java.lang.ref.Reference;
import java.util.concurrent.atomic.AtomicReference;

import src.rhino.circuit.timeout.TimeoutStatus;
import src.rhino.circuit.timeout.TimerListener;

/**
 * @author zhanjun on 2017/6/7.
 */
public class CircuitBreakerContext {
    private final static String TRANSACTION_TYPE = "Rhino.CircuitBreaker";
    private final static String EVENT_PROP_PREFIX = "Rhino.CircuitBreaker.props.";
    private volatile Thread currentThread = Thread.currentThread();
    private RequestStatus requestStatus = RequestStatus.NORMAL;
    private AtomicReference<TimeoutStatus> timeoutStatus = new AtomicReference<>(TimeoutStatus.NOT_EXECUTED);
    private Reference<TimerListener> timerListenerRf = null;
    private AtomicReference<ResultState> resultState = new AtomicReference<>(NOT_STARTED);
    private boolean isTestRequest;

    public CircuitBreakerContext(boolean isTestRequest){
        this.isTestRequest = isTestRequest;
    }

    /**
     * 兼容低版本，部分用户封装熔断器时可能自己创建了context
     */
    public CircuitBreakerContext(){
        this.isTestRequest = false;
    }

    public boolean isTestRequest() {
        return isTestRequest;
    }

    public boolean setRequestTimeout() {
        return timeoutStatus.compareAndSet(TimeoutStatus.NOT_EXECUTED, TimeoutStatus.TIMED_OUT);
    }

    public boolean setRequestComplete() {
        return timeoutStatus.compareAndSet(TimeoutStatus.NOT_EXECUTED, TimeoutStatus.COMPLETED);
    }

    public void clearTimeoutListener() {
        if (timerListenerRf != null) {
            timerListenerRf.clear();
            timerListenerRf = null;
        }
    }

    public boolean markSuccess() {
        return resultState.compareAndSet(NOT_STARTED, SUCCESS);
    }

    public boolean markFailed() {
        return resultState.compareAndSet(NOT_STARTED, FAILED);
    }

    public boolean markComplete() {
        return resultState.compareAndSet(SUCCESS, COMPLETE) || resultState.compareAndSet(FAILED, COMPLETE);
    }

    public void interrupt() {
        if (currentThread != null) {
            currentThread.interrupt();
        }
    }

    public void clearInterrupted() {
        Thread.interrupted();
    }

    public RequestStatus getRequestStatus() {
        return requestStatus;
    }

    public TimeoutStatus getTimeoutStatus() {
        return timeoutStatus.get();
    }

    public void setRequestStatus(RequestStatus requestStatus) {
        this.requestStatus = requestStatus;
    }

    public void setTimerListenerRf(Reference<TimerListener> timerListenerRf) {
        this.timerListenerRf = timerListenerRf;
    }

    public void setCurrentThread() {
        this.currentThread = Thread.currentThread();
    }

    public void start(CircuitBreakerProperties properties) {
       /* Cat.logBatchTransaction(TRANSACTION_TYPE, properties.getRhinoKey(), 1, 0, 0);
        Cat.logEvent(EVENT_PROP_PREFIX + "ForceOpen", String.valueOf(properties.getIsForceOpen()));
        Cat.logEvent(EVENT_PROP_PREFIX + "Timeout", String.valueOf(properties.getTimeoutInMilliseconds()));
        Cat.logEvent(EVENT_PROP_PREFIX + "ErrorThresholdPercentage", String.valueOf(properties.getErrorThresholdPercentage()));
        Cat.logEvent(EVENT_PROP_PREFIX + "ErrorThresholdCount", String.valueOf(properties.getErrorThresholdCount()));
        Cat.logEvent(EVENT_PROP_PREFIX + "RequestVolumeThreshold", String.valueOf(properties.getRequestVolumeThreshold()));*/
    }

    protected enum ResultState {
        NOT_STARTED, SUCCESS, FAILED, COMPLETE
    }
}
