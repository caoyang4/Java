package src.rhino.circuit.forceopen;

import src.rhino.circuit.CircuitBreakerProperties;
import src.rhino.circuit.RequestStatus;

/**
 * @description: ForceOpenDegradeStrategy
 * @author: zhangxiudong
 * @date: 2019-12-02
 **/
public interface ForceOpenDegradeStrategy {

    RequestStatus getRequestStatus();

    class Factory {

        public static ForceOpenDegradeStrategy create(CircuitBreakerProperties circuitBreakerProperties) {
            ForceOpenDegradeStrategy forceOpenDegradeStrategy;
            try {
                forceOpenDegradeStrategy = Type.get(circuitBreakerProperties.getForceOpenDegradeStrategy()).create(circuitBreakerProperties);
            } catch (Exception e) {
                System.out.println("手动降级策略初始化失败，采用默认降级策略" + e);
                forceOpenDegradeStrategy = Type.NoOp.create(circuitBreakerProperties);
            }
            return new ForceOpenDegradeStrategyProxy(forceOpenDegradeStrategy, circuitBreakerProperties);
        }
    }

    enum Type {

        NoOp(0) {
            @Override
            public NoOpForceOpenDegradeStrategy create(CircuitBreakerProperties circuitBreakerProperties) {
                return new NoOpForceOpenDegradeStrategy(circuitBreakerProperties);
            }
        },

        Percent(1) {
            @Override
            public PercentForceOpenDegradeStrategy create(CircuitBreakerProperties circuitBreakerProperties) {
                return new PercentForceOpenDegradeStrategy(circuitBreakerProperties);
            }
        };

        Type(int code) {
            this.code = code;
        }

        private int code;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public abstract ForceOpenDegradeStrategy create(CircuitBreakerProperties circuitBreakerProperties);

        public static Type get(int code) {
            switch (code) {
                case 1:
                    return Percent;
                default:
                    return NoOp;
            }
        }
    }


}
