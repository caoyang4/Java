package src.rhino.onelimiter.strategy;

import src.rhino.onelimiter.ExecuteResult;
import src.rhino.onelimiter.OneLimiterStrategy;
import src.rhino.onelimiter.strategy.pid.PIDSystemMetricObserver;
import src.rhino.system.SystemMetricCollector;
import src.rhino.system.SystemMetrics;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @description: 单机自适应限流策略PID控制算法
 * @author: zhangxiudong
 * @date: 2021-05-31
 **/
public class AdaptiveVmPIDStrategy implements LimiterStrategy {

    static {
        //初始化系统指标计算
        new SystemMetricCollector();
    }

    private OneLimiterStrategy strategy;

    private static volatile double threshold = 0.6D;

    private static volatile double probability = 1.0D;

    public AdaptiveVmPIDStrategy(OneLimiterStrategy strategy) {
        this.strategy = strategy;
        if (strategy.getThreshold() > 0) {
            AdaptiveVmPIDStrategy.threshold = strategy.getThreshold() / 100D;
            PIDSystemMetricObserver.setCpuTarget(AdaptiveVmPIDStrategy.threshold);
        }
    }

    @Override
    public ExecuteResult execute(String entrance, Map<String, String> reqParams, int tokens) {
        if (strategy.getThresholdTypeEnum().getCpuUsage() < threshold) {
            return ExecuteResult.CONTINUE;
        }
        boolean pass = ThreadLocalRandom.current().nextDouble() < probability;
        if (pass) {
            return ExecuteResult.CONTINUE;
        } else {
            return ExecuteResult.createRejectResult(strategy.getCode(), strategy.getMsg());
        }

    }

    public static double getProbability() {
        return AdaptiveVmPIDStrategy.probability;
    }

    public static void setProbability(double probability) {
        AdaptiveVmPIDStrategy.probability = probability;
    }


    public enum ThresholdTypeEnum {
        /**
         * 平均值
         */
        AVERAGE() {
            @Override
            public double getCpuUsage() {
                return SystemMetrics.getAverageCpuUsage();
            }
        },
        /**
         * 最大值
         */
        MAX() {
            @Override
            public double getCpuUsage() {
                return SystemMetrics.getMaxCpuUsage();
            }
        },
        /**
         * 最小值
         */
        MIN() {
            @Override
            public double getCpuUsage() {
                return SystemMetrics.getMinCpuUsage();
            }
        },
        /**
         * TOP_PERCENT
         */
        TOP_PERCENT() {
            @Override
            public double getCpuUsage() {
                return SystemMetrics.getTopPercent();
            }
        },
        /**
         * 当前值
         */
        CURRENT() {
            @Override
            public double getCpuUsage() {
                return SystemMetrics.getCurrentCpuUsage();
            }
        };

        public abstract double getCpuUsage();

    }
}
