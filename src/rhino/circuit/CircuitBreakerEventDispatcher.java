package src.rhino.circuit;

import src.rhino.dispatcher.AbstractEventDispatcher;

/**
 * @author zhanjun on 2017/08/17.
 */
public class CircuitBreakerEventDispatcher extends AbstractEventDispatcher {

    public CircuitBreakerEventDispatcher(String rhinoKey) {
        super(rhinoKey);
    }

    /**
     *
     * @param rhinoKey
     * @return
     */
    public static CircuitBreakerEventDispatcher create(String rhinoKey) {
        return new CircuitBreakerEventDispatcher(rhinoKey);
    }
}
