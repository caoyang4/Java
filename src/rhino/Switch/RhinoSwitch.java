package src.rhino.Switch;

import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.util.ClassUtils;

import src.rhino.config.AppKey;
import src.rhino.config.ConfigFactory;
import src.rhino.service.RhinoManager;
import src.rhino.util.AppUtils;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

/**
 * Created by zhanjun on 2018/4/16.
 */
public class RhinoSwitch {

    private static final Logger logger = LoggerFactory.getLogger(RhinoSwitch.class);
    private static String selfAppKey = AppUtils.getAppName();
    private static final HashFunction hashFunc = Hashing.murmur3_32();
    private static ConcurrentHashMap<String, Integer> switchBuckets = new ConcurrentHashMap<>();

    static {
        List<String> appKeys = SpringFactoriesLoader.loadFactoryNames(AppKey.class, ClassUtils.getDefaultClassLoader());
        for (String appKey : appKeys) {
            try {
                ConfigFactory.getInstance(appKey);
            } catch (Exception e) {
                logger.error("AppKey Configuration create failed, appKey={}", appKey, e);
            }
        }
    }


    /**
     * @param key
     * @param defaultValue
     * @return
     */
    public static String getString(String key, String defaultValue) {
        return getString(selfAppKey, key, defaultValue);
    }

    /**
     * @param appKey
     * @param key
     * @param defaultValue
     * @return
     */
    public static String getString(String appKey, String key, String defaultValue) {
        RhinoSwitchProperties properties = RhinoSwitchProperties.Factory.getInstance(appKey, key);
        String value = properties.getString(defaultValue);
        register(appKey, properties.getFullKey(), properties.getConfigName(), key, value);
        return value;
    }

    /**
     * @param key
     * @param defaultValue
     * @return
     */
    public static int getInt(String key, int defaultValue) {
        return getInt(selfAppKey, key, defaultValue);
    }

    /**
     * @param appKey
     * @param key
     * @param defaultValue
     * @return
     */
    public static int getInt(String appKey, String key, int defaultValue) {
        RhinoSwitchProperties properties = RhinoSwitchProperties.Factory.getInstance(appKey, key);
        int value = properties.getInt(defaultValue);
        register(appKey, properties.getFullKey(), properties.getConfigName(), key, value);
        return value;
    }

    /**
     * @param key
     * @param defaultValue
     * @return
     */
    public static long getLong(String key, long defaultValue) {
        return getLong(selfAppKey, key, defaultValue);
    }

    /**
     * @param appKey
     * @param key
     * @param defaultValue
     * @return
     */
    public static long getLong(String appKey, String key, long defaultValue) {
        RhinoSwitchProperties properties = RhinoSwitchProperties.Factory.getInstance(appKey, key);
        long value = properties.getLong(defaultValue);
        register(appKey, properties.getFullKey(), properties.getConfigName(), key, value);
        return value;
    }

    /**
     * @param key
     * @param defaultValue
     * @return
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        return getBoolean(selfAppKey, key, defaultValue);
    }

    /**
     * @param appKey
     * @param key
     * @param defaultValue
     * @return
     */
    public static boolean getBoolean(String appKey, String key, boolean defaultValue) {
        RhinoSwitchProperties properties = RhinoSwitchProperties.Factory.getInstance(appKey, key);
        boolean value = properties.getBoolean(defaultValue);
        register(appKey, properties.getFullKey(), properties.getConfigName(), key, value);
        return value;
    }

    /**
     * 注册开关
     *
     * @param appKey
     * @param fullKey
     * @param key
     * @param value
     */
    private static void register(String appKey, String fullKey, String configName, String key, Object value) {
        Integer bucket = switchBuckets.get(fullKey);
        if (bucket != null) {
//            Cat.logEvent("Rhino.Switch.Bucket-" + bucket, key + "=" + value);
            return;
        }
        int num = Math.abs(hashFunc.newHasher().putString(key, Charset.defaultCharset()).hash().asInt() % 5);
        Object oldValue = switchBuckets.putIfAbsent(fullKey, num);
        if (oldValue == null) {
            SwitchEntity entity = new SwitchEntity(appKey, key, value.toString());
            entity.setConfigName(configName);
            RhinoManager.reportSwitch(entity);
        }
    }
}