package src.rhino.circuit.listener;

import src.rhino.circuit.CircuitBreakerListener;

/**
 * Created by zhanjun on 2018/3/3.
 */
public class CircuitBreakerNoOpListener implements CircuitBreakerListener {

    @Override
    public void circuitBreakerOpened(CircuitBreakerListenerContext listenerContext) {
        //Do nothing
    }

    @Override
    public void circuitBreakerClosed(CircuitBreakerListenerContext listenerContext) {
        //Do nothing

    }
}
