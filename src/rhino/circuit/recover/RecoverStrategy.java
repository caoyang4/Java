package src.rhino.circuit.recover;

import src.rhino.circuit.CircuitBreakerProperties;
import src.rhino.circuit.RequestStatus;

/**
 * @author zhanjun on 2017/6/16.
 */
public interface RecoverStrategy {

    /**
     * reset the time point to resume normal request
     */
    void reset();

    /**
     * get the current percentage of normal request
     * @return
     */
    long getPercent();

    /**
     * method to resume normal request
     *
     * @param percentage
     * @return
     */
    RequestStatus doRecover(long percentage);

    class Factory {

        /**
         * create recoverStrategy with specified strategy type
         *
         * @param properties
         * @return
         */
        public static RecoverStrategy create(CircuitBreakerProperties properties) {
            RecoverStrategy recoverStrategy = Type.get(properties.getRecoverStrategy()).create(properties);
            return new RecoverStrategyProxy(recoverStrategy, properties);
        }
    }

    enum Type {

        NoOpRecover(1) {
            @Override
            public RecoverStrategy create(CircuitBreakerProperties properties) {
                return new NoOpRecoverStrategy(properties);
            }
        },

        SmoothRecover(2) {
            @Override
            public RecoverStrategy create(CircuitBreakerProperties properties) {
                return new SmoothRecoverStrategy(properties);
            }
        },

        FastRecover(3) {
            @Override
            public RecoverStrategy create(CircuitBreakerProperties properties) {
                return new FastRecoverStrategy(properties);
            }
        },

        TimeRecover(4) {
            @Override
            public RecoverStrategy create(CircuitBreakerProperties properties) {
                return new TimeRecoverStrategy(properties);
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

        public abstract RecoverStrategy create(CircuitBreakerProperties properties);

        public static Type get(int code) {
            switch (code) {
                case 1: return NoOpRecover;
                case 3: return FastRecover;
                case 4: return TimeRecover;
                default: return SmoothRecover;
            }
        }
    }
}
