package src.rhino.Switch;

import java.util.concurrent.ConcurrentHashMap;

import src.rhino.RhinoProperties;
import src.rhino.util.Preconditions;

/**
 * Created by zhen on 2019/1/10.
 */
public interface RhinoSwitchProperties extends RhinoProperties {

    String configKeySuffix = "props";

    /**
     * 是否启用
     * @return
     */
    boolean isActive();

    /**
     * full key
     * @return
     */
    String getFullKey();

    /**
     *
     * @param defaultValue
     * @return
     */
    String getString(String defaultValue);

    /**
     *
     * @param defaultValue
     * @return
     */
    int getInt(int defaultValue);

    /**
     *
     * @param defaultValue
     * @return
     */
    long getLong(long defaultValue);


    /**
     *
     * @param defaultValue
     * @return
     */
    boolean getBoolean(boolean defaultValue);

    /**
     *
     * @return
     */
    String getConfigName();


    class Factory {
        private static final ConcurrentHashMap<String, RhinoSwitchProperties> rhinoSwitchPropertiesHolder = new ConcurrentHashMap<>();

        public static RhinoSwitchProperties getInstance(String appKey, String key) {
            Preconditions.checkNotNull(appKey, "Rhino Switch: app key can not be null");
            Preconditions.checkNotNull(key, "Rhino Switch: key can not be null");
            String fullKey = appKey + "." + key;
            RhinoSwitchProperties rhinoSwitchProperties = rhinoSwitchPropertiesHolder.get(fullKey);
            if (rhinoSwitchProperties == null) {
                synchronized (RhinoSwitchProperties.class) {
                    rhinoSwitchProperties = rhinoSwitchPropertiesHolder.get(fullKey);
                    if (rhinoSwitchProperties == null) {
                        rhinoSwitchProperties = new DefaultRhinoSwitchProperties(appKey, key);
                        rhinoSwitchPropertiesHolder.put(fullKey, rhinoSwitchProperties);
                        rhinoSwitchProperties.addConfigChangedListener(null);
                    }
                }
            }
            return rhinoSwitchProperties;
        }

    }
}
