package src.rhino.config;

/**
 * Created by zhanjun on 2018/4/17.
 */
public class LionConfigBuilder implements ConfigBuilder {

    @Override
    public Configuration create(String appKey) {
        return new LionConfig();
    }
}
