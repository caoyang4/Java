package src.rhino.onelimiter;

import src.rhino.onelimiter.cluster.strategy.NettyFrequencyStrategy;
import src.rhino.onelimiter.cluster.strategy.NettyLimiterStrategy;
import src.rhino.onelimiter.strategy.*;

/**
 * Created by zhanjun on 2018/4/14.
 */
public enum OneLimiterStrategyEnum {

    /**
     * 黑白名单策略
     */
    WHITE_BLACK_SET("WHITE_BLACK_SET", 0) {
        @Override
        protected LimiterStrategy getLimiter(OneLimiterStrategy strategy) {
            return new WhiteBlackStrategy(strategy);
        }
    },

    /**
     * 用户百分比策略
     */
    USER_PERCENTAGE("USER_PERCENTAGE", 1) {
        @Override
        protected LimiterStrategy getLimiter(OneLimiterStrategy strategy) {
            return new UserPercentageStrategy(strategy);
        }
    },

    /**
     * 单机总限流
     */
    SINGLE_VM_QPS_ALL("SINGLE_VM_QPS_ALL", 2) {
        @Override
        protected LimiterStrategy getLimiter(OneLimiterStrategy strategy) {
            return new SingleVmStrategy(strategy);
        }
    },

    /**
     * 集群限频
     */
    CLUSTER_FREQUENCY("CLUSTER_FREQUENCY", 3) {
        @Override
        protected LimiterStrategy getLimiter(OneLimiterStrategy strategy) {
            return new ClusterFrequencyStrategy(strategy);
        }
    },

    /**
     * 集群配额
     */
    CLUSTER_QUOTA("CLUSTER_QUOTA", 4) {
        @Override
        protected LimiterStrategy getLimiter(OneLimiterStrategy strategy) {
            return new ClusterFrequencyStrategy(strategy);
        }
    },

    /**
     * 单机限流
     */
    SINGLE_VM_QPS("SINGLE_VM_QPS", 5) {
        @Override
        protected LimiterStrategy getLimiter(OneLimiterStrategy strategy) {
            return new SingleVmStrategy(strategy);
        }
    },

    /**
     * 集群限流，使用redis计数实现
     */
    CLUSTER_QPS("CLUSTER_QPS", 6) {
        @Override
        protected LimiterStrategy getLimiter(OneLimiterStrategy strategy) {
            return new ClusterRateStrategy(strategy);
        }
    },

    /**
     * 集群非精确限流，使用单机限流实现，配置的总QPS平均分配到各个主机
     */
    CLUSTER_QPS_VM("CLUSTER_QPS_VM", 6) {
        @Override
        protected LimiterStrategy getLimiter(OneLimiterStrategy strategy) {
            return new SingleVmStrategy(strategy);
        }

        @Override
        public OneLimiterStrategyEnum getRealType() {
            return CLUSTER_QPS;
        }
    },

    /**
     * 集中式集群限流
     */
    CENTER_CLUSTER_QPS("CENTER_CLUSTER_QPS", 7) {
        @Override
        protected LimiterStrategy getLimiter(OneLimiterStrategy strategy) {
            return new CenterClusterRateStrategy(strategy);
        }
    },
	/**
	 * 集群精确限流2.0
	 */
	CLUSTER_QPS_V2("CLUSTER_QPS_V2", 8) {
        @Override
        protected LimiterStrategy getLimiter(OneLimiterStrategy strategy) {
            return new NettyLimiterStrategy(strategy);
        }
        
        @Override
        public OneLimiterStrategyEnum getRealType() {
            return CLUSTER_QPS;
        }
    },
	/**
	 * 集群限频2.0
	 */
	CLUSTER_FREQUENCY_V2("CLUSTER_FREQUENCY_V2", 9) {
        @Override
        protected LimiterStrategy getLimiter(OneLimiterStrategy strategy) {
        	return new NettyFrequencyStrategy(strategy);
        }
        
        @Override
        public OneLimiterStrategyEnum getRealType() {
            return CLUSTER_FREQUENCY;
        }
    },
    /**
     * 自适应限流
     */
    ADAPTIVE_VM_PID("ADAPTIVE_VM_PID", 10) {
        @Override
        protected LimiterStrategy getLimiter(OneLimiterStrategy strategy) {
            return new AdaptiveVmPIDStrategy(strategy);
        }
    };



    String name;

    /**
     * 策略执行顺序，值小的先执行
     */
    int order;

    public String getName() {
        return name;
    }

    public OneLimiterStrategyEnum getRealType() {
        return this;
    }

    OneLimiterStrategyEnum(String name, int order) {
        this.name = name;
        this.order = order;
    }

    protected int getOrder() {
        return order;
    }

    protected abstract LimiterStrategy getLimiter(OneLimiterStrategy strategy);
}
