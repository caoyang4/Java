package src.rhino.config;

import java.util.concurrent.ConcurrentHashMap;

import src.rhino.util.AppUtils;
import src.rhino.util.ExtensionLoader;

/**
 * Created by zhanjun on 2017/6/19.
 */
public class ConfigFactory {

    private static ConfigBuilder configBuilder = ExtensionLoader.getExtension(ConfigBuilder.class);
    private static ConcurrentHashMap<String, Configuration> configHolder = new ConcurrentHashMap();
    // 项目本身的配置（由于mcc配置中心不同的appKey需要不同的client）
    private static Configuration configuration;


    /**
     * 默认是Lion
     * 只有引用了rhino-config-mcc包，才使用mcc
     */
    static {
        if (configBuilder == null) {
            configBuilder = new LionConfigBuilder();
        }
        configuration = configBuilder.create(AppUtils.getAppName());
    }

    /**
     * 返回本项目中的配置中心client
     * @return
     */
    public static Configuration getInstance() {
        return configuration;
    }

    /**
     *
     * @return
     */
    public static Configuration getLion() {
        return new LionConfigBuilder().create(AppUtils.getAppName());
    }

    /**
     *
     * @param appKey
     * @return
     */
    public static Configuration getInstance(String appKey) {
        Configuration config = configHolder.get(appKey);
        if (config == null) {
            config = configBuilder.create(appKey);
            Configuration oldConfig = configHolder.putIfAbsent(appKey, config);
            if (oldConfig != null) {
                config = oldConfig;
            }
        }
        return config;
    }
}
