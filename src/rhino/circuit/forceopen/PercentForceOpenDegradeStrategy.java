package src.rhino.circuit.forceopen;

import java.util.concurrent.ThreadLocalRandom;

import src.rhino.circuit.CircuitBreakerProperties;
import src.rhino.circuit.RequestStatus;

/**
 * @description: NoOpForceOpenDegrade
 * @author: zhangxiudong
 * @date: 2019-12-02
 **/
public class PercentForceOpenDegradeStrategy implements ForceOpenDegradeStrategy {

    private int percent;

    public PercentForceOpenDegradeStrategy(CircuitBreakerProperties circuitBreakerProperties) {
        this.percent = circuitBreakerProperties.getForceOpenDegradePercent();
    }

    @Override
    public RequestStatus getRequestStatus() {
        return ThreadLocalRandom.current().nextInt(100) < percent ?
                RequestStatus.DEGRADE : RequestStatus.NORMAL;
    }
}
