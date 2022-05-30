package src.rhino.onelimiter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mysql.cj.util.StringUtils;
import org.springframework.util.CollectionUtils;

import src.rhino.config.ConfigChangedListener;
import src.rhino.config.ConfigFactory;
import src.rhino.config.Configuration;
import src.rhino.exception.RhinoOneLimiterInitException;
import src.rhino.log.Logger;
import src.rhino.log.LoggerFactory;
import src.rhino.util.AppUtils;
//import src.rhino.util.SerializerUtils;

/**
 * Created by zhanjun on 2018/4/13.
 */
public class OneLimiterManager {

    private static Logger logger = LoggerFactory.getLogger(OneLimiterManager.class);
    private static final String MainConfigInitType = "Rhino.OneLimiter.MainConfig.Init";
    private static final String MainConfigChangeType = "Rhino.OneLimiter.MainConfig.Change";
    private static final String EntranceConfigInitType = "Rhino.OneLimiter.EntranceConfig.Init";
    private static final String EntranceConfigChangeType = "Rhino.OneLimiter.EntranceConfig.change";

    private String rhinoKey;
    private volatile OneLimiterConfig oneLimiterConfig;

    /**
     * <configKey, rule>
     */
    private final Map<String, OneLimiterRule> oneLimiterRules = new ConcurrentHashMap<>();

    /**
     * <configKey, pattern>
     */
    private final Map<String, Pattern> regexPathMap = new ConcurrentHashMap<>();

    /**
     * <entrance, configKey>
     */
    private final Map<String, String> normalPathMap = new ConcurrentHashMap<>();

    private final String RHINO_ONE_LIMITER_KEYS = AppUtils.getAppName() + ".rhino.oneLimiter.keys";
    private final String RHINO_ONE_LIMITER_STRATEGY = AppUtils.getAppName() + ".rhino.oneLimiter.strategy";
    private final String RHINO_ONE_LIMITER_CONFIG = AppUtils.getAppName() + ".rhino.oneLimiter.config";
    private static Configuration config = ConfigFactory.getInstance();

    //更新限流规则的线程池
    private static ExecutorService updateWorker = Executors.newFixedThreadPool(1, new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "Rhino-OneLimiter-UpdateWorker");
            thread.setDaemon(true);
            return thread;
        }
    });

    public OneLimiterManager(String rhinoKey) {
        this.rhinoKey = rhinoKey;
        registerMainConfigListener(); //监听全局配置
        initMainConfig(); //初始化全局配置

        handleOneLimiterKeys(); // 获取限流配置
    }

    /**
     * 强制使用lion作为配置中心
     */
    public static void useLion() {
        if (!config.isLion()) {
            config = ConfigFactory.getLion();
        }
    }

    /**
     * 初始化MainConfig
     */
    private void initMainConfig() {
        String message = MainConfigInitType + " " + rhinoKey;
        try {
            String configKey = RHINO_ONE_LIMITER_CONFIG + "." + rhinoKey;
            String configValue = config.getStringValue(configKey, "");
            message += " " + configValue;
            updateMainConfig(configValue);
            logger.info(message);
        } catch (Exception e) {
            Exception error = new RhinoOneLimiterInitException(e);
            logger.error(message, error);
        }
    }

    /**
     * 监听MainConfig
     */
    private void registerMainConfigListener() {
        String configKey = RHINO_ONE_LIMITER_CONFIG + "." + rhinoKey;
        ConfigFactory.getInstance().addListener(configKey, new ConfigChangedListener() {
            private Object lock = new Object();

            @Override
            public void invoke(String key, final String oldValue, final String newValue) {
                updateWorker.submit(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (lock) {
                            updateMainConfigWrapper(oldValue, newValue);
                        }
                    }
                });
            }
        });
    }

    /**
     * 封装更新MainConfig，增加打点、日志
     *
     * @param configJsonStr
     */
    private void updateMainConfigWrapper(String oldConfigJsonStr, String newConfigJsonStr) {
        String message = MainConfigInitType + " " + rhinoKey + " " + "oldValue=" + oldConfigJsonStr + "&newValue=" + newConfigJsonStr;
        try {
            updateMainConfig(newConfigJsonStr);
            logger.info(message);
        } catch (Exception e) {
            Exception error = new RhinoOneLimiterInitException(e);
            logger.error(message, error);
        }
    }

    /**
     * 更新MainConfig
     *
     * @param configJsonStr
     * @param warm          是否预热，避免更新后突发限流
     */
    private void updateMainConfig(String configJsonStr) throws Exception {
        if (StringUtils.isNullOrEmpty(configJsonStr)) {
            this.oneLimiterConfig = null;
            return;
        }

//        OneLimiterConfig mainConfig = SerializerUtils.read(configJsonStr, OneLimiterConfig.class);
//        mainConfig.init(rhinoKey);
//        this.oneLimiterConfig = mainConfig;
    }

    /**
     * 根据RHINO_ONE_LIMITER_KEYS中的值，监听每一个规则
     */
    private void handleOneLimiterKeys() {
        String configKey = RHINO_ONE_LIMITER_KEYS + "." + rhinoKey;
        config.addListener(configKey, new ConfigChangedListener() {
            private Object lock = new Object();

            @Override
            public void invoke(String key, final String oldValue, final String newValue) {
                updateWorker.submit(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (lock) {
                            updateEntranceList(oldValue, newValue);
                        }
                    }
                });
            }
        });

        //监听并初始化EntranceConfig
        String keys = config.getStringValue(configKey, "");
        if (StringUtils.isNullOrEmpty(keys)) {
            return;
        }
        String[] flowLimiterKeys = keys.split(",");
        for (String key : flowLimiterKeys) {
            handleStrategyAndAddListener(key);
        }
    }

    /**
     * Entrance注册表更新回调
     *
     * @param oldValue
     * @param newValue
     */
    private void updateEntranceList(String oldValue, String newValue) {
        Set<String> oldKeySet;
        if (StringUtils.isNullOrEmpty(oldValue)) {
            oldKeySet = new HashSet<>(Arrays.asList(oldValue.split(",")));
        } else {
            oldKeySet = new HashSet<>();
        }

        if (StringUtils.isNullOrEmpty(newValue)) {
            return;
        }

        // 只监听新增的key，删除的key可以不处理，如果一个key被多次删除添加，可能存在被多次注册监听的情况
        for (String newKey : newValue.split(",")) {
            if (oldKeySet.contains(newKey)) {
                continue;
            }
            handleStrategyAndAddListener(newKey);
        }
    }

    /**
     * 记录各个策略的注册状态，防止多次注册（删除之后又添加的情况）
     */
    private Map<String, Boolean> configChangedListenerStatus = new HashMap<>();

    /**
     * @param key
     * @return
     */
    private void handleStrategyAndAddListener(String key) {
        String configKey = RHINO_ONE_LIMITER_STRATEGY + "." + rhinoKey + "." + key;
        String value = config.getStringValue(configKey, "");
        Boolean isRegister = configChangedListenerStatus.get(configKey);
        // 不存在并发问题
        if (isRegister == null) {
            configChangedListenerStatus.put(configKey, Boolean.TRUE);
            ConfigChangedListener configChangedListener = new ConfigChangedListener() {
                private Object lock = new Object();

                @Override
                public void invoke(final String key, final String oldValue, final String newValue) {
                    updateWorker.submit(new Runnable() {
                        @Override
                        public void run() {
                            synchronized (lock) {
                                updateEntranceConfigWrapper(key, oldValue, newValue);
                            }
                        }
                    });
                }
            };
            ConfigFactory.getInstance().addListener(configKey, configChangedListener);
            handleOneLimiterRule(configKey, value);
        }
    }

    private void updateEntranceConfigWrapper(String key, String oldValue, String newValue) {
        try {
            // 删除
            if (StringUtils.isNullOrEmpty(newValue)) {
                oneLimiterRules.remove(key);
                regexPathMap.remove(key);
                // 移除对应的entrance
                String entrance = null;
                for (Map.Entry<String, String> entry : normalPathMap.entrySet()) {
                    if (entry.getValue().equals(key)) {
                        entrance = entry.getKey();
                        break;
                    }
                }

                if (entrance != null) {
                    normalPathMap.remove(entrance);
                    logger.info(EntranceConfigChangeType + " remove key=" + key);
                }
                return;
            }
            //更新
            handleOneLimiterRule(key, newValue);
        } finally {
        }
    }


    /**
     * @param key
     * @param value
     */
    private void handleOneLimiterRule(String key, String value) {
        if (StringUtils.isNullOrEmpty(value)) {
            return;
        }
        String name = "unknown";
        String valuePairs = "key=" + key + "&value=" + value;
       /* try {
            OneLimiterRule oneLimiterRule = SerializerUtils.read(value, OneLimiterRule.class);
            String entrance = oneLimiterRule.getEntrance();
            name = rhinoKey + "#" + entrance;
            oneLimiterRule.init(rhinoKey);
            if (oneLimiterRule.isRegex()) {
                regexPathMap.put(key, Pattern.compile(entrance));
            } else {
                normalPathMap.put(entrance, key);
            }
            oneLimiterRules.put(key, oneLimiterRule);
            logger.info(EntranceConfigInitType + " " + name + " " + valuePairs);
        } catch (Exception e) {
            Exception error = new RhinoOneLimiterInitException(e);
            String message = EntranceConfigInitType + " " + name + " " + valuePairs;
            logger.error(message, error);
        }*/
    }

    /**
     * @return
     */
    public boolean isActive() {
        return oneLimiterConfig != null && oneLimiterConfig.isActive() && oneLimiterConfig.isInGrayList();
    }

    /**
     * 是否忽略压测流量
     *
     * @return
     */
    public boolean isTestRequestIgnored() {
        return oneLimiterConfig != null && oneLimiterConfig.isIgnoreTestRequest();
    }

    /**
     * 是否为debug模式
     *
     * @return
     */
    public boolean isDebug() {
        return oneLimiterConfig != null && oneLimiterConfig.isDebug();
    }


    public List<OneLimiterStrategy> getStrategyList(String path, Map<String, String> reqParams) {
        return getStrategyList(path, reqParams, false);
    }


    /**
     * 根据path获取限流策略
     *
     * @param path      entrance
     * @param reqParams 业务参数
     * @param isTest    是否是压测流量
     * @return
     */
    public List<OneLimiterStrategy> getStrategyList(String path, Map<String, String> reqParams, boolean isTestRequest) {
        List<OneLimiterStrategy> oneLimiterStrategyList = new ArrayList<>();

        // 添加黑白名单策略
        OneLimiterStrategy whiteBlackStrategy = oneLimiterConfig.getWhiteBlackStrategy(isTestRequest);
        if (whiteBlackStrategy != null) {
            oneLimiterStrategyList.add(whiteBlackStrategy);
        }

        // 添加用户百分比
        OneLimiterStrategy userPercentageStrategy = oneLimiterConfig.getUserPercentageStrategy(isTestRequest);
        if (userPercentageStrategy != null) {
            oneLimiterStrategyList.add(userPercentageStrategy);
        }

        // 添加Entrance策略
        getEntranceStrategyList(path, reqParams, isTestRequest, oneLimiterStrategyList);

        //添加主机限流策略
        OneLimiterStrategy singleVmQps = oneLimiterConfig.getSingleVmQps(isTestRequest);
        if (singleVmQps != null) {
            oneLimiterStrategyList.add(singleVmQps);
        }
        //添加自适应限流策略
        OneLimiterStrategy adaptiveVmPIDStrategy = oneLimiterConfig.getAdaptiveVmPIDStrategy();
        if (adaptiveVmPIDStrategy != null) {
            oneLimiterStrategyList.add(adaptiveVmPIDStrategy);
        }

        return oneLimiterStrategyList;
    }

    /**
     * 匹配Entrance限流规则
     * 顺序：应用限流规则 > 带参数的限流规则 > 不带参数的限流规则
     *
     * @param path
     * @param reqParams
     * @param isTestRquest
     * @param strategyList
     */
    private void getEntranceStrategyList(String path, Map<String, String> reqParams, boolean isTestRequest, List<OneLimiterStrategy> strategyList) {
        // appKey级别限流
        if (!CollectionUtils.isEmpty(reqParams)) {
            String appKey = reqParams.get(OneLimiterConstants.APPKEY);
            if (StringUtils.isNullOrEmpty(appKey)) {
                String key1 = normalPathMap.get(appKey);
                getEffectiveStrategy(key1, strategyList, isTestRequest);
            }
        }

        // 匹配正常路径
        String key0 = normalPathMap.get(path);
        getEffectiveStrategy(key0, strategyList, isTestRequest);

        // 匹配正则路径
        for (Map.Entry<String, Pattern> entry : regexPathMap.entrySet()) {
            Matcher matcher = entry.getValue().matcher(path);
            if (matcher.matches()) {
                getEffectiveStrategy(entry.getKey(), strategyList, isTestRequest);
            }
        }
    }

    /**
     * @param key
     * @param oneLimiterStrategyList
     */
    private void getEffectiveStrategy(String key, List<OneLimiterStrategy> oneLimiterStrategyList, boolean isTestRequest) {
        if (key == null) {
            return;
        }
        OneLimiterRule flowLimiterRule = oneLimiterRules.get(key);
        if (flowLimiterRule != null) {
            List<OneLimiterStrategy> strategies = flowLimiterRule.getStrategyList(isTestRequest);
            if (!CollectionUtils.isEmpty(strategies)) {
                for (OneLimiterStrategy strategy : strategies) {
                    if (strategy.isActive()) {
                    	Boolean active = oneLimiterConfig.getStrategyToSwitch().get(strategy.getStrategyEnum().getRealType());
                        if (active != null && active) {
                            oneLimiterStrategyList.add(strategy);
                        }
                    }
                }
            }
        }
    }


    public OneLimiterConfig getOneLimiterConfig() {
        return oneLimiterConfig;
    }

    public static Configuration getConfig() {
        return config;
    }
}
