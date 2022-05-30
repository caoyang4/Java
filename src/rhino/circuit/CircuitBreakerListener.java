package src.rhino.circuit;

import src.rhino.circuit.listener.CircuitBreakerListenerContext;

/**
 * @author zhanjun
 */
public interface CircuitBreakerListener {

    /**
     * do this method when circuit breaker is open
     * @param listenerContext
     */
    void circuitBreakerOpened(CircuitBreakerListenerContext listenerContext);

    /**
     * do this method when circuit breaker is close
     * @param listenerContext
     */
    void circuitBreakerClosed(CircuitBreakerListenerContext listenerContext);
}
