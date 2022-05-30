package src.rhino.circuit.degrade;

import src.rhino.circuit.CircuitBreakerProperties;

/**
 * Created by zhanjun on 2018/3/29.
 */
public interface DegradeStrategy {

    /**
     * 降级实现逻辑
     *
     * @return
     */
    Object degrade() throws Exception;

    /**
     * 是否默认
     * @return
     */
    boolean isDefault();

    class Factory {

        /**
         * create degradeStrategy with specified strategy type
         *
         * @param properties
         * @return
         */
        public static DegradeStrategy create(CircuitBreakerProperties properties) {
            DegradeStrategy degradeStrategy;
            try {
                degradeStrategy = Type.get(properties.getDegradeStrategy()).create(properties);
            } catch (Exception e) {
                // 如果降级策略初始化失败，就用默认的降级策略，避免引起熔断器初始化失败
                degradeStrategy = Type.DEFAULT.create(properties);
            }
            return new DegradeStrategyProxy(degradeStrategy, properties);
        }
    }

    enum Type {

        DEFAULT(0) {
            @Override
            DegradeStrategy create(CircuitBreakerProperties properties) {
                return new DefaultDegradeStrategy(properties);
            }
        },

        CONSTANT(1) {
            @Override
            DegradeStrategy create(CircuitBreakerProperties properties) {
                return new ConstantDegradeStrategy(properties);
            }
        },

        EXCEPTION(2) {
            @Override
            DegradeStrategy create(CircuitBreakerProperties properties) {
                return new ExceptionDegradeStrategy(properties);
            }
        },


        GROOVY(3) {
            @Override
            DegradeStrategy create(CircuitBreakerProperties properties) {
                return new GroovyDegradeStrategy(properties);
            }
        },

        MOCK(4) {
            @Override
            DegradeStrategy create(CircuitBreakerProperties properties) {
                return null;
            }
        };

        private int code;

        Type(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        abstract DegradeStrategy create(CircuitBreakerProperties properties);

        static Type get(int code) {
            switch (code) {
                case 0: return DEFAULT;
                case 1: return CONSTANT;
                case 2: return EXCEPTION;
                case 3: return GROOVY;
                case 4: return MOCK;
                default: return DEFAULT;
            }
        }
    }
}
