package src.rhino.config;

/**
 * @author zhanjun
 */
public interface ConfigChangedListener {

    /**
     * 监听配置中心的属性变动
     * @param key
     * @param oldValue
     * @param newValue
     */
    void invoke(String key, String oldValue, String newValue);
}
