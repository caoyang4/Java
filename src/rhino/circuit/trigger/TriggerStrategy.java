package src.rhino.circuit.trigger;

import src.rhino.circuit.CircuitBreakerProperties;
import src.rhino.metric.HealthCountSummary;

/**
 * @author zhanjun on 2017/7/4.
 */
public interface TriggerStrategy {

    /**
     * check trigger to open circuit breaker
     *
     * @param health
     * @return
     */
    boolean trigger(HealthCountSummary health);

    class Factory {

        /**
         * create recoverStrategy with specified strategy type
         *
         * @param properties
         * @return
         */
        public static TriggerStrategy create(CircuitBreakerProperties properties) {
            TriggerStrategy triggerStrategy = TriggerStrategy.Type.get(properties.getTriggerStrategy()).create(properties);
            return new TriggerStrategyProxy(triggerStrategy, properties);
        }
    }

    enum Type {

        DEFAULT(1) {
            @Override
            TriggerStrategy create(CircuitBreakerProperties properties) {
                return new DefaultTriggerStrategy(properties);
            }
        };

        private int code;

        Type(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        abstract TriggerStrategy create(CircuitBreakerProperties properties);

        static Type get(int code) {
            switch (code) {
                case 1: return DEFAULT;
                default: return DEFAULT;
            }
        }
    }
}
