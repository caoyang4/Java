package src.rhino.circuit.component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


import com.mysql.cj.util.StringUtils;
import src.rhino.config.ConfigChangedListener;
import src.rhino.config.ConfigFactory;
import src.rhino.config.Configuration;
import src.rhino.log.Logger;
import src.rhino.log.LoggerFactory;
import src.rhino.util.SerializerUtils;

/**
 * 中间件熔断器管理
 * 在平台实现按需配置
 * Created by zmz on 2020/2/11.
 */
public abstract class ComponentCircuitBreakerManager {

    private final static Logger logger = LoggerFactory.getLogger(ComponentCircuitBreakerManager.class);

    protected static final String CAT_TRANSACTION = "Rhino.CircuiBreaker.ComponentCircuiBreaker";

    private Configuration configuration;
    private String configKey;

    /**
     * 记录已经接入熔断器的类或方法（二选一）
     *
     * 例如
     * {
     *     "rhino-demo-service1": [],
     *     "rhino-demo-service2": ["method1"]
     * }
     * 假设应用rhino-demo下公有service1，service2，service3三个服务
     * 对于service1，开启了服务熔断器（配置了service，并且没有method记录）
     * 对于service2，只有方法method1开启动的方法熔断器（配置了service，并且存在对应method记录）
     * 对于service3，没有开启服务熔断器和方法熔断器（service没有被配置）
     */
    protected volatile Map<String, Set<String>> configInfo;

    protected String appkey;

    public ComponentCircuitBreakerManager(String appKey){
        this.appkey = appKey;
        this.configKey = getComponentConfigKey();
        this.configuration = ConfigFactory.getInstance();
        init();
    }

    private void init(){
        String value = configuration.getStringValue(configKey, "");
        initOrUpdateConfig(value);
        postConfigInit();
        addConfigListener();
    }

    private void addConfigListener() {
        configuration.addListener(configKey, new ConfigChangedListener() {
            private Object lock = new Object();

            @Override
            public void invoke(String key, String oldValue, String newValue) {
                synchronized (lock) {
                    //更新客户端配置
                    initOrUpdateConfig(newValue);
                    //更新配置后回调
                    postConfigChange(oldValue, newValue);
                }
            }
        });
    }

    /**
     * Cat打点的Name
     * @return
     */
    protected abstract String getCatTransactionName();

    /**
     * 拼接中间件客户端配置的key
     * @param appKey
     * @return
     */
    protected abstract String getComponentConfigKey();

    /**
     * 初始化中间件配置后回调
     */
    protected abstract void postConfigInit();

    /**
     * 更新配置后的回调
     * @param value
     */
    protected abstract void postConfigChange(String oldValue, String newValue);

    /**
     * 读取中间件配置
     * @param value
     */
    protected void initOrUpdateConfig(String value){
        if(StringUtils.isNullOrEmpty(value)){
            this.configInfo = new HashMap<>();
            return;
        }

/*        try{
            ObjectMapper mapper = SerializerUtils.custom();
            JavaType configJavaType = mapper.getTypeFactory().constructParametricType(HashMap.class, String.class, HashSet.class);
            Map<String, Set<String>> newConfig = mapper.readValue(value, configJavaType);
            this.configInfo = newConfig;
        }catch (Exception e){
            logger.error("component config read error: " + value, e);
            Cat.logError(e);
        }*/
    }
}
