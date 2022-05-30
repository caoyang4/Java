package src.rhino.config;

/**
 * Created by zhanjun on 2018/4/17.
 */
public interface ConfigBuilder {

    /**
     * 配置中心客户端生成器
     * @param appKey
     * @return
     */
    Configuration create(String appKey);
}
