package src.rhino.config;

/**
 * Created by zhanjun on 2017/5/8.
 */
public class LionConfig implements Configuration {

    @Override
    public boolean isLion() {
        return true;
    }

    @Override
    public String getName() {
        return "LION";
    }

    @Override
    public String getStringValue(String property, String defaultValue) {
        return defaultValue;
    }

    @Override
    public int getIntValue(String property, int defaultValue) {
        return defaultValue;
    }

    @Override
    public boolean getBooleanValue(String property, boolean defaultValue) {
        return defaultValue;
    }

    @Override
    public long getLongValue(String property, long defaultValue) {
        return defaultValue;
    }

    @Override
    public float getFloatValue(String property, float defaultValue) {
        return defaultValue;
    }

    @Override
    public void addListener(final String property, final ConfigChangedListener listener) {
        /*Lion.addConfigListener(property, new ConfigListener() {
            @Override
            public void configChanged(ConfigEvent configEvent) {
                listener.invoke(property, configEvent.getOldValue(), configEvent.getValue());
            }
        });*/
    }
}
