package src.rhino.config;

/**
 * Created by zhanjun on 2017/5/8.
 */
public interface Configuration {

    /**
     * @return
     */
    boolean isLion();

    /**
     * Gets config name: MCC or LION
     *
     * @return
     */
    String getName();

    /**
     * Gets the string associated with property string. If not found it will return defaultValue.
     *
     * @param property
     * @param defaultValue
     * @return
     */
    String getStringValue(String property, String defaultValue);

    /**
     * Gets the boolean associated with property string. If not found it will return defaultValue.
     *
     * @param property
     * @param defaultValue
     * @return
     */
    int getIntValue(String property, int defaultValue);

    /**
     * Gets the boolean associated with property string. If not found it will return defaultValue.
     *
     * @param property
     * @param defaultValue
     * @return
     */
    boolean getBooleanValue(String property, boolean defaultValue);

    /**
     * Gets the long associated with property string. If not found it will return defaultValue.
     *
     * @param property
     * @param defaultValue
     * @return
     */
    long getLongValue(String property, long defaultValue);

    /**
     * Gets the float associated with property string. If not found it will return defaultValue.
     *
     * @param property
     * @param defaultValue
     * @return
     */
    float getFloatValue(String property, float defaultValue);

    /**
     * add property changed listener
     *
     * @param property
     * @param listener
     */
    void addListener(String property, ConfigChangedListener listener);

}
