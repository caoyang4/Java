package src.rhino.circuit.degrade;

import src.rhino.circuit.CircuitBreakerProperties;
import src.rhino.circuit.CircuitBreakerPropertyData;
import src.rhino.config.PropertyChangedListener;
import src.rhino.log.Logger;
import src.rhino.log.LoggerFactory;

/**
 * Created by zhanjun on 2018/3/29.
 */
public class DegradeStrategyProxy extends PropertyChangedListener<CircuitBreakerPropertyData> implements DegradeStrategy {

    private static final Logger logger = LoggerFactory.getLogger(DegradeStrategyProxy.class);
    private volatile DegradeStrategy degradeStrategy;
    private CircuitBreakerProperties circuitBreakerProperties;

    public DegradeStrategyProxy(DegradeStrategy degradeStrategy, CircuitBreakerProperties circuitBreakerProperties) {
        this.degradeStrategy = degradeStrategy;
        this.circuitBreakerProperties = circuitBreakerProperties;
        circuitBreakerProperties.addPropertyChangedListener(this);
    }

    @Override
    public Object degrade() throws Exception {
        return degradeStrategy.degrade();
    }

    @Override
    public boolean isDefault() {
        return degradeStrategy.isDefault();
    }

    @Override
    public void trigger(CircuitBreakerPropertyData oldProperty, CircuitBreakerPropertyData newProperty) {
        int newStrategy = newProperty.getDegradeStrategy();
        String newStrategyValue = newProperty.getDegradeStrategyValue();

        // 两种情况，需要重新初始化话降级策略
        // 1、策略类型变化
        // 2、策略内容变化
        if (oldProperty.getDegradeStrategy() != newStrategy ||
                (newStrategyValue != null && !newStrategyValue.equals(oldProperty.getDegradeStrategyValue()))) {
            try {
                this.degradeStrategy = Type.get(newStrategy).create(this.circuitBreakerProperties);
            } catch (Exception e) {
                logger.error("降级策略更新异常", e);
            }
        }
    }
}
