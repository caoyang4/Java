package src.rhino.onelimiter;

import java.util.Map;
import java.util.Set;

import org.springframework.util.CollectionUtils;

import src.rhino.util.AppUtils;

/**
 * 单机限流主配置，支持版本
 * active：  1.2.1.1
 * ignoreTestRequest： 1.2.3
 * grayList： 1.2.3
 * strategyToSwitch： 1.2.1.1
 * singleVmQps：1.2.1.1
 * whiteBlackStrategy： 1.2.1.1
 * userPercentageStrategy： 1.2.1.1
 * debug： 1.2.5
 * testSingeleVmQps: 1.2.5
 * testWhiteBlackStrategy: 1.2.5
 * testUserPercentageStrategy: 1.2.5
 *
 * Created by zhanjun on 2018/4/13.
 */
public class OneLimiterConfig {

    private static String localIP = AppUtils.getLocalIp();

    /**
     * 总开关
     */
    private boolean active;

    /**
     * 压测流量开关
     * 默认false：不忽略压测流量
     * true：压测流量直接通过，不统计
     */
    private boolean ignoreTestRequest;

    /**
     * 灰度机器ip列表，如果为空，则不灰度
     */
    private Set<String> grayList;

    /**
     * 策略开关
     */
    private Map<OneLimiterStrategyEnum, Boolean> strategyToSwitch;

    /**
     * 单机总限流策略
     */
    private OneLimiterStrategy singleVmQps;

    /**
     * 单机总限流策略（压测）
     */
    private OneLimiterStrategy testSingleVmQps;

    /**
     * 黑白名单策略
     */
    private OneLimiterStrategy whiteBlackStrategy;

    /**
     * 黑白名单策略（压测）
     */
    private OneLimiterStrategy testWhiteBlackStrategy;

    /**
     * 用户百分比策略
     */
    private OneLimiterStrategy userPercentageStrategy;

    /**
     * 用户百分比策略（压测）
     */
    private OneLimiterStrategy testUserPercentageStrategy;
    /**
     * 自适应限流策略
     */
    private OneLimiterStrategy adaptiveVmPIDStrategy;
    /**
     * 是否灰度生效
     */
    private boolean isInGrayList;

    /**
     * 是否debug模式，即只告警，不限流
     */
    private boolean debug;

    /**
     * 全局策略初始化
     *
     * @param rhinoKey
     */
    public void init(String rhinoKey) {
        initTestStrategy(rhinoKey);
        initNormalStrategy(rhinoKey);

        this.isInGrayList = grayList == null || grayList.isEmpty() || grayList.contains(localIP);
    }

    /**
     * 初始化（压测）黑白名单策略、用户百分比策略、单机总限流策略
     * 如果没有配置就复制相应的正常策略
     *
     * @param rhinoKey
     */
    private void initTestStrategy(String rhinoKey) {
        //黑白名单策略暂未开放压测配置，直接拷贝正常策略配置
        if (testWhiteBlackStrategy == null && whiteBlackStrategy != null) {
            testWhiteBlackStrategy = OneLimiterStrategy.copy(whiteBlackStrategy, false);
        }

        if (testWhiteBlackStrategy != null) {
            if (testWhiteBlackStrategy.isActive() && (!CollectionUtils.isEmpty(testWhiteBlackStrategy.getWhiteList()) || !CollectionUtils.isEmpty(testWhiteBlackStrategy.getBlackList()))) {
                testWhiteBlackStrategy.setTest(true);
                testWhiteBlackStrategy.init(rhinoKey);
            } else {
                testWhiteBlackStrategy = null;
            }
        }

        if (testUserPercentageStrategy == null && userPercentageStrategy != null) {
            testUserPercentageStrategy = OneLimiterStrategy.copy(userPercentageStrategy, false);
        }

        if (testUserPercentageStrategy != null) {
            if (testUserPercentageStrategy.isActive()) {
                testUserPercentageStrategy.setTest(true);
                testUserPercentageStrategy.init(rhinoKey);
            } else {
                testUserPercentageStrategy = null;
            }
        }

        if (testSingleVmQps == null && singleVmQps != null) {
            testSingleVmQps = OneLimiterStrategy.copy(singleVmQps, false);
        }

        if (testSingleVmQps != null) {
            if (testSingleVmQps.isActive()) {
                testSingleVmQps.setTest(true);
                testSingleVmQps.init(rhinoKey);
            } else {
                testSingleVmQps = null;
            }
        }
    }

    /**
     * 初始化（正常）黑白名单策略、用户百分比策略、单机总限流策略
     *
     * @param rhinoKey
     */
    private void initNormalStrategy(String rhinoKey) {
        // 黑白名单策略初始化
        if (whiteBlackStrategy != null) {
            if (whiteBlackStrategy.isActive() && (!CollectionUtils.isEmpty(whiteBlackStrategy.getWhiteList()) || !CollectionUtils.isEmpty(whiteBlackStrategy.getBlackList()))) {
                whiteBlackStrategy.init(rhinoKey);
            } else {
                whiteBlackStrategy = null;
            }
        }

        // 用户百分比策略初始化
        if (userPercentageStrategy != null) {
            if (userPercentageStrategy.isActive()) {
                userPercentageStrategy.init(rhinoKey);
            } else {
                userPercentageStrategy = null;
            }
        }

        // 单机总限流策略初始化
        if (singleVmQps != null) {
            if (singleVmQps.isActive()) {
                singleVmQps.init(rhinoKey);
            } else {
                singleVmQps = null;
            }
        }
        //自适应限流策略初始化
        if (adaptiveVmPIDStrategy != null) {
            if (adaptiveVmPIDStrategy.isActive()) {
                adaptiveVmPIDStrategy.init(rhinoKey);
            } else {
                adaptiveVmPIDStrategy = null;
            }
        }
    }

    /**
     * 验证当前IP是否在灰度列表中
     *
     * @return
     */
    public boolean isInGrayList() {
        return isInGrayList;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isIgnoreTestRequest() {
        return ignoreTestRequest;
    }

    public void setIgnoreTestRequest(boolean ignoreTestRequest) {
        this.ignoreTestRequest = ignoreTestRequest;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public Map<OneLimiterStrategyEnum, Boolean> getStrategyToSwitch() {
        return strategyToSwitch;
    }

    public void setStrategyToSwitch(Map<OneLimiterStrategyEnum, Boolean> strategyToSwitch) {
        this.strategyToSwitch = strategyToSwitch;
    }

    public OneLimiterStrategy getSingleVmQps() {
        return singleVmQps;
    }

    public void setSingleVmQps(OneLimiterStrategy singleVmQps) {
        this.singleVmQps = singleVmQps;
    }

    public OneLimiterStrategy getWhiteBlackStrategy() {
        return whiteBlackStrategy;
    }

    public void setWhiteBlackStrategy(OneLimiterStrategy whiteBlackStrategy) {
        this.whiteBlackStrategy = whiteBlackStrategy;
    }

    public OneLimiterStrategy getUserPercentageStrategy() {
        return userPercentageStrategy;
    }

    public void setUserPercentageStrategy(OneLimiterStrategy userPercentageStrategy) {
        this.userPercentageStrategy = userPercentageStrategy;
    }

    public Set<String> getGrayList() {
        return grayList;
    }

    public void setGrayList(Set<String> grayList) {
        this.grayList = grayList;
    }

    public void setTestSingleVmQps(OneLimiterStrategy testSingleVmQps) {
        this.testSingleVmQps = testSingleVmQps;
    }

    public void setTestWhiteBlackStrategy(OneLimiterStrategy testWhiteBlackStrategy) {
        this.testWhiteBlackStrategy = testWhiteBlackStrategy;
    }

    public void setTestUserPercentageStrategy(OneLimiterStrategy testUserPercentageStrategy) {
        this.testUserPercentageStrategy = testUserPercentageStrategy;
    }

    public OneLimiterStrategy getTestSingleVmQps() {
        return testSingleVmQps;
    }

    public OneLimiterStrategy getTestWhiteBlackStrategy() {
        return testWhiteBlackStrategy;
    }

    public OneLimiterStrategy getTestUserPercentageStrategy() {
        return testUserPercentageStrategy;
    }

    /**
     * 根据压测标识获取黑白名单策略
     *
     * @param isTest
     * @return
     */
    public OneLimiterStrategy getWhiteBlackStrategy(boolean isTestRequest) {
        return isTestRequest ? testWhiteBlackStrategy : whiteBlackStrategy;
    }

    /**
     * 根据压测标识获取用户百分比策略
     *
     * @param isTestRequest
     * @return
     */
    public OneLimiterStrategy getUserPercentageStrategy(boolean isTestRequest) {
        return isTestRequest ? testUserPercentageStrategy : userPercentageStrategy;
    }

    /**
     * 根据压测标识获取单机总限流策略
     *
     * @param isTestRequest
     * @return
     */
    public OneLimiterStrategy getSingleVmQps(boolean isTestRequest) {
        return isTestRequest ? testSingleVmQps : singleVmQps;
    }

    /**
     * 获取自适应限流策略
     *
     * @return
     */
    public OneLimiterStrategy getAdaptiveVmPIDStrategy() {
        return adaptiveVmPIDStrategy;
    }

}
