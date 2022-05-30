package src.rhino.onelimiter;

import src.rhino.dispatcher.RhinoEventDispatcher;
import src.rhino.limit.LimiterEventDispatcher;
import src.rhino.onelimiter.alarm.OneLimiterAlarm;
import src.rhino.onelimiter.matcher.ParamRule;
import src.rhino.onelimiter.strategy.AdaptiveVmPIDStrategy;
import src.rhino.onelimiter.strategy.LimiterStrategy;
import src.rhino.onelimiter.strategy.WhiteBlackEntry;
import src.rhino.util.AppUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 限流策略配置支持版本
 * active：1.2.1.1
 * code：1.2.1.1
 * msg：1.2.1.1
 * timeUnit：1.2.1.1
 * duration：1.2.1.1
 * threshold：1.2.1.1
 * step：1.2.1.1
 * whiteList：1.2.1.1
 * blackList：1.2.1.1
 * paramRules: 1.2.3
 * params: 1.2.1.1 （1.2.3版本后改为paramRules，为了兼容还是需要双写）
 * strategyEnum： 1.2.1.1
 * grayList: 1.2.3
 * debug: 1.2.5.2 debug开关
 * timeout: 1.2.6.1 单机策略超时时间
 * maxBurstSeconds: 1.2.6.1  单机策略最大突发流量
 * testConfiged： 1.2.5
 * testActive： 1.2.5
 * testCode： 1.2.5
 * testMsg： 1.2.5
 * testTimeUnit： 1.2.5
 * testDuration： 1.2.5
 * testThreshold： 1.2.5
 * testStep： 1.2.5
 * testTimeout: 1.2.6.1
 * <p>
 * Created by zhanjun on 2018/4/13.
 */
public class OneLimiterStrategy {

    private static String localIP = AppUtils.getLocalIp();

    private long strategyId;

    /**
     * 当前strategy所属的entrance的id
     */
    private long entranceId;

    /**
     * 策略开关
     */
    private boolean active;

    /**
     * 限流之后的返回码
     */
    private int code;

    /**
     * 限流之后的返回信息
     */
    private String msg;

    /**
     * 限流单位, 秒、分钟、小时、天
     */
    private TimeUnit timeUnit;

    /**
     * 限流单位值
     */
    private int duration;

    /**
     * 限流次数 / 拦截比例 范围[0-100] 单位% 70表示70%的用户会被拒绝访问/自适应限流CPU利用率阈值
     */
    private int threshold;

    /**
     * 集群限流时，为了避免频繁访问redis，一次获取多个token
     */
    private int step;

    /**
     * 白名单集合
     */
    private List<WhiteBlackEntry> whiteList;

    /**
     * 黑名单集合 / 返回空文案的集合
     */
    private List<WhiteBlackEntry> blackList;

    /**
     * 策略参数
     */
    private List<ParamRule> paramRules;

    /**
     * 1.1.8.1版本前简单策略参数
     */
    private Map<String, String> params;

    /**
     * 策略类型
     */
    private OneLimiterStrategyEnum strategyEnum;

    /**
     * 灰度机器ip列表，如果为空，则不灰度
     */
    private Set<String> grayList;

    /**
     * 是否debug模式，即只告警，不限流
     */
    private boolean debug;

    /**
     * 最大突发流量（单机策略）
     */
    private int maxBurstSeconds = 1;

    /**
     * 超时时间（单机策略），毫秒
     */
    private long timeout;

    /**
     * 集群限流计数是否开启SET隔离，低版本默认开启
     *
     * @since 1.2.8
     */
    private boolean setMode = true;

    /**
     * 单机限流、集群非精确限流，是否开启阻塞
     * since 1.2.9
     */
    private boolean block = false;

    /**
     * 限流算法
     * since 1.2.9
     */
    private LimiterAlgorithmEnum limiterAlgorithmEnum = LimiterAlgorithmEnum.SLIDEWINDOW;
    /**
     * 自适应限流，限流阈值触发类型
     * since 1.3.1
     */
    private AdaptiveVmPIDStrategy.ThresholdTypeEnum thresholdTypeEnum = AdaptiveVmPIDStrategy.ThresholdTypeEnum.CURRENT;

    /**
     * 压测策略配置
     */
    private boolean testConfiged;
    private boolean testActive;
    /**
     * 压测流量计数器是否计数正常流量
     * since 1.3.1
     */
    private boolean testMergeNormal = false;
    private int testCode;
    private String testMsg;
    private TimeUnit testTimeUnit;
    private int testDuration;
    private int testThreshold;
    private int testStep;
    private int testMaxBurstSeconds = 1;
    private long testTimeout;

    /*------------------------以上字段需要从配置中心初始化---------------------------------*/

    /**
     * 具体的限流器
     */
    private LimiterStrategy limiter;

    /**
     * 标识
     */
    private String rhinoKey;

    /**
     * Cat上的埋点tag
     */
    private String catEventTag;

    /**
     * 匹配的路径
     */
    private String entrance;

    /**
     * 格式化之后的参数 a=1&b=2
     */
    private String formattedParams;

    /**
     * 事件分发器
     */
    private RhinoEventDispatcher eventDispatcher;

    /**
     * cat transition tag
     */
    private String catTransitionTag;

    /**
     * 告警器
     */
    private OneLimiterAlarm limiterAlarm;

    /**
     * 灰度是否生效
     */
    private boolean isInGrayList;

    /**
     * 是否是压测流量
     */
    private boolean test = false;

    /**
     * 是否是压测策略
     */
    private boolean isTestStrategy = false;
    /**
     * 秒级流量
     */
    private long secondThreshold;

    /**
     * 策略初始化
     * 初始化的时候需要指定是不是压测策略
     *
     * @param rhinoKey
     */
    public void init(String rhinoKey) {
        this.rhinoKey = rhinoKey;
        this.initParamRules();
        this.limiter = strategyEnum.getLimiter(this);
        this.catEventTag = generateCatTag();
        this.eventDispatcher = new LimiterEventDispatcher(rhinoKey);
        this.catTransitionTag = rhinoKey + ".oneLimiter." + strategyEnum.getName();
        this.limiterAlarm = new OneLimiterAlarm(this, eventDispatcher);
        this.isInGrayList = grayList == null || grayList.isEmpty() || grayList.contains(localIP);
        this.secondThreshold = this.getSecondThreshold();
    }

    /**
     * @param path
     * @param reqParams
     * @return
     */
    public ExecuteResult execute(String path, Map<String, String> reqParams, int tokens) {
        // 命中策略的条件
        // 1、灰度名单为空 或者 在灰度名单中 2、参数为空 或者 命中参数
        if (isInGrayList && (formattedParams == null || isParamRulesHit(reqParams))) {
            ExecuteResult result = limiter.execute(path, reqParams, tokens);
            boolean warn = statisticLimitResult(result.getStatus(), tokens);
            // 如果是debug模式，直接忽略，但是前面的打点还是需要的
            if (debug) {
                return ExecuteResult.CONTINUE;
            }

            if (warn) {
                return ExecuteResult.CONTINUE_AND_WARN;
            }

            return result;
        }
        return null;
    }

    /**
     * 根据限流策略结果统计并打点
     *
     * @param status
     * @return
     */
    private boolean statisticLimitResult(ExecuteStatus status, int tokenNum) {
        if (status.isWarn()) {
            //集群限流在策略中已经判断过了
            limiterAlarm.warn(tokenNum);
            return true;
        }

        if (status.isReject()) {
            limiterAlarm.reject(tokenNum);
        } else {
            if (isSingleStrategy()) {
                //单机策略实时检查预警 阈值换为秒级流量
                return limiterAlarm.successAndCheckWarn(secondThreshold, tokenNum);
            } else {
                limiterAlarm.success(tokenNum);
            }
        }
        return false;
    }

    private long getSecondThreshold() {
        if (timeUnit == null) {
            return threshold;
        }
        long periodSecond = timeUnit.toSeconds(duration);
        return threshold / periodSecond;
    }

    /**
     * 是否为单机策略
     *
     * @return
     */
    private boolean isSingleStrategy() {
        return strategyEnum == OneLimiterStrategyEnum.SINGLE_VM_QPS
                || strategyEnum == OneLimiterStrategyEnum.CLUSTER_QPS_VM
                || strategyEnum == OneLimiterStrategyEnum.SINGLE_VM_QPS_ALL;
    }

    /**
     * 验证当前IP是否在灰度列表中
     *
     * @return
     */
    public boolean isInGrayList() {
        return isInGrayList;
    }

    /**
     * 参数是否命中
     *
     * @param reqParams
     * @return
     */
    private boolean isParamRulesHit(Map<String, String> reqParams) {
        // 进入到这个方法，说明该策略定义在参数规则下面
        // 如果请求中没有参数，则不可能会命中
        for (ParamRule paramRule : paramRules) {
            // 只要有一个条件不满足，就算没命中
            if (!paramRule.match(reqParams)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 格式化参数
     *
     * @return
     */
    private void initParamRules() {
        if (!CollectionUtils.isEmpty(paramRules)) {
            StringBuilder formatParams = new StringBuilder("{");
            for (ParamRule paramRule : paramRules) {
                // 参数规则初始化
                paramRule.init();
                if (formatParams.length() > 1) {
                    formatParams.append("&");
                }
                formatParams.append(paramRule.getName());
                formatParams.append(" ");
                formatParams.append(paramRule.getMatchMode().getSymbol());
                formatParams.append(" ");
                formatParams.append(paramRule.getPattern());
            }
            formatParams.append("}");
            this.formattedParams = formatParams.toString();
        }
    }

    /**
     * 生成cat的埋点tag
     * 1、rhinoKey.singleVmQps_All
     * 2、rhinoKey.singleVmQps./home/*.#{a == 1&b == 2}
     *
     * @return
     */
    private String generateCatTag() {
        StringBuilder catTag = new StringBuilder(strategyEnum.getName());
        if (entrance != null) {
            catTag.append(".").append(entrance);
            if (formattedParams != null) {
                catTag.append("#").append(formattedParams);
            }
        }
        return catTag.toString();
    }


    public long getStrategyId() {
        return strategyId;
    }

    public void setStrategyId(long strategyId) {
        this.strategyId = strategyId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public String getEntrance() {
        return entrance;
    }

    public void setEntrance(String entrance) {
        this.entrance = entrance;
    }

    public String getRhinoKey() {
        return rhinoKey;
    }

    public void setRhinoKey(String rhinoKey) {
        this.rhinoKey = rhinoKey;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public long getEntranceId() {
        return entranceId;
    }

    public void setEntranceId(long entranceId) {
        this.entranceId = entranceId;
    }

    public OneLimiterStrategyEnum getStrategyEnum() {
        return strategyEnum;
    }

    public void setStrategyEnum(OneLimiterStrategyEnum strategyEnum) {
        this.strategyEnum = strategyEnum;
    }

    public String getFormattedParams() {
        return formattedParams;
    }

    public List<WhiteBlackEntry> getWhiteList() {
        return whiteList;
    }

    public void setWhiteList(List<WhiteBlackEntry> whiteList) {
        this.whiteList = whiteList;
    }

    public List<WhiteBlackEntry> getBlackList() {
        return blackList;
    }

    public void setBlackList(List<WhiteBlackEntry> blackList) {
        this.blackList = blackList;
    }

    public List<ParamRule> getParamRules() {
        return paramRules;
    }

    public void setParamRules(List<ParamRule> paramRules) {
        this.paramRules = paramRules;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public Set<String> getGrayList() {
        return grayList;
    }

    public void setGrayList(Set<String> grayList) {
        this.grayList = grayList;
    }

    public String getCatEventTag() {
        return catEventTag;
    }

    public void setCatEventTag(String catEventTag) {
        this.catEventTag = catEventTag;
    }

    public OneLimiterAlarm getLimiterAlarm() {
        return limiterAlarm;
    }

    public boolean isTest() {
        return test;
    }

    public void setTest(boolean test) {
        this.test = test;
    }

    public boolean isTestStrategy() {
        return isTestStrategy;
    }

    public void setTestStrategy(boolean isTestStrategy) {
        this.isTestStrategy = isTestStrategy;
    }

    public boolean isTestStrategySetMerged() {
        return isTestStrategy && testMergeNormal;
    }

    public boolean isTestMergeNormal() {
        return testMergeNormal;
    }

    public void setTestMergeNormal(boolean testMergeNormal) {
        this.testMergeNormal = testMergeNormal;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{")
                .append("\"strategyEnum\":\"").append(strategyEnum).append("\",")
                .append("\"active\":").append(active).append(",")
                .append("\"threshold\":").append(threshold).append(",")
                .append("\"duration\":").append(duration).append(",")
                .append("\"timeUnit\":\"").append(timeUnit).append("\",")
                .append("\"code\":").append(code).append(",")
                .append("\"msg\":\"").append(msg).append("\",")
                .append("\"block\":\"").append(block).append("\",")
                .append("\"limiterAlgorithmEnum\":\"").append(limiterAlgorithmEnum).append("\",")
                .append("\"timeout\":").append(timeout).append(",")
                .append("\"maxBurstSeconds\":").append(maxBurstSeconds).append(",")
                .append("\"step\":").append(step).append(",")
                .append("\"testConfiged\":").append(testConfiged).append(",")
                .append("\"testActive\":").append(testActive).append(",")
                .append("\"testThreshold\":").append(testThreshold).append(",")
                .append("\"testDuration\":").append(testDuration).append(",")
                .append("\"testTimeUnit\":\"").append(testTimeUnit).append("\",")
                .append("\"testCode\":").append(testCode).append(",")
                .append("\"testMsg\":\"").append(testMsg).append("\",")
                .append("\"testTimeout\":").append(testTimeout).append(",")
                .append("\"testMaxBurstSeconds\":").append(testMaxBurstSeconds).append(",")
                .append("\"testStep\":").append(testStep).append(",")
                .append("\"testMergeNormal\":").append(testMergeNormal).append(",")
                .append("\"thresholdTypeEnum\":").append(thresholdTypeEnum)
                .append("}");
        return builder.toString();
    }

    public boolean isTestConfiged() {
        return testConfiged;
    }

    public void setTestConfiged(boolean testConfiged) {
        this.testConfiged = testConfiged;
    }

    public int getTestCode() {
        return testCode;
    }

    public void setTestCode(int testCode) {
        this.testCode = testCode;
    }

    public String getTestMsg() {
        return testMsg;
    }

    public void setTestMsg(String testMsg) {
        this.testMsg = testMsg;
    }

    public TimeUnit getTestTimeUnit() {
        return testTimeUnit;
    }

    public void setTestTimeUnit(TimeUnit testTimeUnit) {
        this.testTimeUnit = testTimeUnit;
    }

    public int getTestDuration() {
        return testDuration;
    }

    public void setTestDuration(int testDuration) {
        this.testDuration = testDuration;
    }

    public int getTestThreshold() {
        return testThreshold;
    }

    public void setTestThreshold(int testThreshold) {
        this.testThreshold = testThreshold;
    }

    public int getTestStep() {
        return testStep;
    }

    public void setTestStep(int testStep) {
        this.testStep = testStep;
    }

    public boolean isTestActive() {
        return testActive;
    }

    public void setTestActive(boolean testActive) {
        this.testActive = testActive;
    }

    public int getMaxBurstSeconds() {
        return maxBurstSeconds;
    }

    public void setMaxBurstSeconds(int maxBurstSeconds) {
        this.maxBurstSeconds = maxBurstSeconds;
    }

    public int getTestMaxBurstSeconds() {
        return testMaxBurstSeconds;
    }

    public void setTestMaxBurstSeconds(int testMaxBurstSeconds) {
        this.testMaxBurstSeconds = testMaxBurstSeconds;
    }


    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public long getTestTimeout() {
        return testTimeout;
    }

    public void setTestTimeout(long testTimeout) {
        this.testTimeout = testTimeout;
    }

    public boolean isSetMode() {
        return setMode;
    }

    public void setSetMode(boolean setMode) {
        this.setMode = setMode;
    }

    public boolean isBlock() {
        return block;
    }

    public void setBlock(boolean block) {
        this.block = block;
    }

    public LimiterAlgorithmEnum getLimiterAlgorithmEnum() {
        return limiterAlgorithmEnum;
    }

    public void setLimiterAlgorithmEnum(LimiterAlgorithmEnum limiterAlgorithmEnum) {
        this.limiterAlgorithmEnum = limiterAlgorithmEnum;
    }

    public AdaptiveVmPIDStrategy.ThresholdTypeEnum getThresholdTypeEnum() {
        return thresholdTypeEnum;
    }

    public void setThresholdTypeEnum(AdaptiveVmPIDStrategy.ThresholdTypeEnum thresholdTypeEnum) {
        this.thresholdTypeEnum = thresholdTypeEnum;
    }

    /**
     * 拷贝限流策略
     *
     * @param source
     * @param isTestConfig true-拷贝压测策略，false-拷贝正常策略
     * @return
     */
    public static OneLimiterStrategy copy(OneLimiterStrategy source, boolean isTestConfig) {
        OneLimiterStrategy target = new OneLimiterStrategy();
        target.setStrategyId(source.getStrategyId());
        target.setParamRules(source.getParamRules());
        target.setParams(source.getParams());
        target.setStrategyEnum(source.getStrategyEnum());
        target.setWhiteList(source.getWhiteList());
        target.setGrayList(source.getGrayList());
        target.setDebug(source.isDebug());
        target.setSetMode(source.isSetMode());
        target.setBlock(source.isBlock());
        target.setLimiterAlgorithmEnum(source.getLimiterAlgorithmEnum());
        target.setTestMergeNormal(source.isTestMergeNormal());
        target.setThresholdTypeEnum(source.getThresholdTypeEnum());

        if (isTestConfig) {
            target.setActive(source.isTestActive());
            target.setCode(source.getTestCode());
            target.setMsg(source.getTestMsg());
            target.setThreshold(source.getTestThreshold());
            target.setDuration(source.getTestDuration());
            target.setTimeUnit(source.getTestTimeUnit());
            target.setStep(source.getTestStep());
            target.setMaxBurstSeconds(source.getTestMaxBurstSeconds());
            target.setTimeout(source.getTestTimeout());
        } else {
            target.setActive(source.isActive());
            target.setCode(source.getCode());
            target.setMsg(source.getMsg());
            target.setThreshold(source.getThreshold());
            target.setDuration(source.getDuration());
            target.setTimeUnit(source.getTimeUnit());
            target.setStep(source.getStep());
            target.setMaxBurstSeconds(source.getMaxBurstSeconds());
            target.setTimeout(source.getTimeout());
        }
        return target;
    }
}
