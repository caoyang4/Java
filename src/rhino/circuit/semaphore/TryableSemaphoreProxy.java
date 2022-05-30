package src.rhino.circuit.semaphore;

import src.rhino.circuit.CircuitBreakerProperties;
import src.rhino.circuit.CircuitBreakerPropertyData;
import src.rhino.config.PropertyChangedListener;

/**
 * Created by zhanjun on 2018/4/3.
 */
public class TryableSemaphoreProxy extends PropertyChangedListener<CircuitBreakerPropertyData> implements TryableSemaphore {

    private TryableSemaphore tryableSemaphore;

    public TryableSemaphoreProxy(CircuitBreakerProperties circuitBreakerProperties) {
        this.tryableSemaphore = new TryableSemaphoreImpl(circuitBreakerProperties.getSemaphorePermits());
        circuitBreakerProperties.addPropertyChangedListener(this);
    }

    @Override
    public boolean tryAcquire() {
        return tryableSemaphore.tryAcquire();
    }

    @Override
    public void release() {
        tryableSemaphore.release();
    }

    @Override
    public int getNumberOfPermitsUsed() {
        return tryableSemaphore.getNumberOfPermitsUsed();
    }

    @Override
    public void setPermits(int numberOfPermits) {
        //Do nothing
    }

    @Override
    public void trigger(CircuitBreakerPropertyData oldProperty, CircuitBreakerPropertyData newProperty) {
        int newCount = newProperty.getSemaphorePermits() <= 0 ? 0 : newProperty.getSemaphorePermits();
        int oldCount = oldProperty.getSemaphorePermits() <= 0 ? 0 : oldProperty.getSemaphorePermits();
        if (oldCount != newCount) {
            tryableSemaphore.setPermits(newCount);
        }
    }
}
