package src.rhino.circuit.forceopen;

import src.rhino.circuit.CircuitBreakerProperties;
import src.rhino.circuit.RequestStatus;

/**
 * @description: NoOpForceOpenDegrade
 * @author: zhangxiudong
 * @date: 2019-12-02
 **/
public class NoOpForceOpenDegradeStrategy implements ForceOpenDegradeStrategy {

    public NoOpForceOpenDegradeStrategy(CircuitBreakerProperties circuitBreakerProperties) {
    }

    @Override
    public RequestStatus getRequestStatus() {
        return RequestStatus.DEGRADE;
    }
}
