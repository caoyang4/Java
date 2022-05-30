package src.rhino.circuit.semaphore;

import src.rhino.circuit.CircuitBreakerProperties;

/**
 * Created by zhanjun on 2018/4/3.
 */
public interface TryableSemaphore {

    /**
     *  try to get a permit
     * @return
     */
    boolean tryAcquire();

    /**
     *  release permit
     */
    void release();

    /**
     * reset number of permits
     * @param numberOfPermits
     */
    void setPermits(int numberOfPermits);

    /**
     *
     * @return
     */
    int getNumberOfPermitsUsed();


    class Factory {

        public static TryableSemaphore create(CircuitBreakerProperties properties) {
            return new TryableSemaphoreProxy(properties);
        }
    }
}
