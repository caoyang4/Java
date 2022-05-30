package src.rhino.circuit.forceopen;

import src.rhino.circuit.CircuitBreakerProperties;
import src.rhino.circuit.CircuitBreakerPropertyData;
import src.rhino.circuit.RequestStatus;
import src.rhino.config.PropertyChangedListener;
import src.rhino.log.Logger;
import src.rhino.log.LoggerFactory;

/**
 * @description: ForceOpenDegradeStrategyProxy
 * @author: zhangxiudong
 * @date: 2019-12-10
 **/
public class ForceOpenDegradeStrategyProxy extends PropertyChangedListener<CircuitBreakerPropertyData> implements ForceOpenDegradeStrategy {

    private static final Logger logger = LoggerFactory.getLogger(ForceOpenDegradeStrategyProxy.class);
    private volatile ForceOpenDegradeStrategy forceOpenDegradeStrategy;
    private CircuitBreakerProperties circuitBreakerProperties;


    public ForceOpenDegradeStrategyProxy(ForceOpenDegradeStrategy forceOpenDegradeStrategy, CircuitBreakerProperties circuitBreakerProperties) {
        this.forceOpenDegradeStrategy = forceOpenDegradeStrategy;
        this.circuitBreakerProperties = circuitBreakerProperties;
        circuitBreakerProperties.addPropertyChangedListener(this);
    }

    @Override
    public RequestStatus getRequestStatus() {
        return this.forceOpenDegradeStrategy.getRequestStatus();
    }

    @Override
    public void trigger(CircuitBreakerPropertyData oldProperty, CircuitBreakerPropertyData newProperty) {
        if (oldProperty.getForceOpenDegradeStrategy() == newProperty.getForceOpenDegradeStrategy()
                && oldProperty.getForceOpenDegradePercent() == newProperty.getForceOpenDegradePercent()) {
            return;
        }
        this.forceOpenDegradeStrategy = ForceOpenDegradeStrategy.Type.get(this.circuitBreakerProperties.getForceOpenDegradeStrategy()).create(circuitBreakerProperties);

    }
}
