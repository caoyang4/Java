package src.rhino.retry.delay;

import src.rhino.retry.RetryContext;
import src.rhino.retry.RetryProperties;
import src.rhino.retry.RetryPropertiesBean;

/**
 * Created by zhen on 2019/2/20.
 */
public interface BackOffStrategy {

    /**
     * retry 休眠处理
     *
     * @param retryContext
     */
    void backOff(RetryContext retryContext);

    class Factory {
        public static BackOffStrategy create(RetryProperties retryProperties) {
            BackOffStrategy backOffStrategy = Type.getType(retryProperties.getDelayStrategy()).create(retryProperties);
            return new BackOffStrategyProxy(backOffStrategy, retryProperties);
        }
    }


    enum Type {

        /**
         * 立即retry
         */
        NoOpBackOff(0) {
            @Override
            public BackOffStrategy create(RetryProperties retryProperties) {
                return new NoOpBackOffStrategy(retryProperties);
            }

            @Override
            boolean isPropertiesChanged(RetryPropertiesBean oldOne, RetryPropertiesBean newOne) {
                return false;
            }
        },
        /**
         * 固定间隔后retry，默认
         */
        FixedBackOff(1) {
            @Override
            public BackOffStrategy create(RetryProperties retryProperties) {
                return new FixedBackOffStrategy(retryProperties);
            }

            @Override
            boolean isPropertiesChanged(RetryPropertiesBean oldOne, RetryPropertiesBean newOne) {
                return oldOne.getDelay() != newOne.getDelay();
            }
        },
        /**
         * 固定范围随机间隔
         */
        RandomBackOff(2) {
            @Override
            public BackOffStrategy create(RetryProperties retryProperties) {
                return new RandomRangeBackOffStrategy(retryProperties);
            }

            @Override
            boolean isPropertiesChanged(RetryPropertiesBean oldOne, RetryPropertiesBean newOne) {
                return oldOne.getMinDelay() != newOne.getMinDelay() || oldOne.getMaxDelay() != newOne.getMaxDelay();
            }
        },
        /**
         * 指数级
         */
        ExponentialBackOff(3) {
            @Override
            public BackOffStrategy create(RetryProperties retryProperties) {
                return new ExponentialBackOffStrategy(retryProperties);
            }

            @Override
            boolean isPropertiesChanged(RetryPropertiesBean oldOne, RetryPropertiesBean newOne) {
                return oldOne.getDelay() != newOne.getDelay() || oldOne.getMaxDelay() != newOne.getMaxDelay() || Double.compare(oldOne.getMultiplier(), newOne.getMultiplier()) != 0;
            }
        },
        /**
         * 指数级随机
         */
        ExponentialRandomBackOff(4) {
            @Override
            public BackOffStrategy create(RetryProperties retryProperties) {
                return new ExponentialRandomBackOffStrategy(retryProperties);
            }

            @Override
            boolean isPropertiesChanged(RetryPropertiesBean oldOne, RetryPropertiesBean newOne) {
                return oldOne.getDelay() != newOne.getDelay() || oldOne.getMaxDelay() != newOne.getMaxDelay() || Double.compare(oldOne.getMultiplier(), newOne.getMultiplier()) != 0;
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

        public abstract BackOffStrategy create(RetryProperties retryProperties);

        abstract boolean isPropertiesChanged(RetryPropertiesBean oldOne, RetryPropertiesBean newOne);

        public static Type getType(int code) {
            switch (code) {
                case 0:
                    return NoOpBackOff;
                case 1:
                    return FixedBackOff;
                case 2:
                    return RandomBackOff;
                case 3:
                    return ExponentialBackOff;
                case 4:
                    return ExponentialRandomBackOff;
                default:
                    return FixedBackOff;
            }
        }
    }
}
