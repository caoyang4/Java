package src.rhino.circuit;

/**
 * @author zhanjun on 2017/4/21.
 */
public class NoOpCircuitBreaker implements CircuitBreaker {

    public static final CircuitBreaker INSTANCE = new NoOpCircuitBreaker();

    @Override
    public boolean isEnable() {
        return false;
    }

    @Override
    public Status getStatus() {
        return Status.CLOSE;
    }

    @Override
    public boolean allowRequest() {
        return true;
    }

    @Override
    public boolean allowRequest(CircuitBreakerContext circuitBreakerContext) {
        return true;
    }

    @Override
    public CircuitBreakerContext getCircuitBreakerContext() {
        return null;
    }

    @Override
    public void setSuccess() {
        //Do nothing

    }

    @Override
    public void setSuccess(CircuitBreakerContext circuitBreakerContext) {
        //Do nothing

    }

    @Override
    public boolean setFailed(Throwable throwable) {
        return true;
    }

    @Override
    public boolean setFailed(Throwable throwable, CircuitBreakerContext circuitBreakerContext) {
        return true;
    }

    @Override
    public void complete() {
        //Do nothing

    }

    @Override
    public void setCircuitBreakerListener(CircuitBreakerListener... circuitBreakerListener) {
        //Do nothing

    }

    @Override
    public boolean isFallbackOnException() {
        return false;
    }

    @Override
    public void markReject() {
        //Do nothing
    }

    @Override
    public boolean isDefaultDegrade() {
        return false;
    }

    @Override
    public Object handleDegrade() {
        return null;
    }

    @Override
    public CircuitBreakerProperties getCircuitBreakerProperties() {
        return null;
    }
}
