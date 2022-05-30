
package src.rhino;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mysql.cj.util.StringUtils;

import src.rhino.annotation.JsonIgnore;
import src.rhino.config.ConfigChangedListener;
import src.rhino.config.ConfigFactory;
import src.rhino.config.Configuration;
import src.rhino.config.PropertyChangedListener;
import src.rhino.log.Logger;
import src.rhino.log.LoggerFactory;
import src.rhino.util.SerializerUtils;

/**
 * @author zhanjun on 2017/5/9.
 */
public abstract class RhinoConfigProperties {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    protected String appKey;
    protected RhinoType rhinoType;
    protected String rhinoKey;
    protected Configuration configuration;
    protected List<PropertyChangedListener> propertyChangedListeners = new ArrayList<>();

    public RhinoConfigProperties(String appKey, String rhinoKey, RhinoType rhinoType, Configuration config) {
        this.appKey = appKey;
        this.rhinoKey = rhinoKey;
        this.rhinoType = rhinoType;
        this.configuration = (config == null) ? ConfigFactory.getInstance() : config;
    }

    public void addPropertyChangedListener(PropertyChangedListener listener) {
        propertyChangedListeners.add(listener);
    }

    public String getAppKey() {
        return appKey;
    }

    public RhinoType getRhinoType() {
        return rhinoType;
    }

    public String getRhinoKey() {
        return rhinoKey;
    }

    public String getStringValue(String name, String defaultValue) {
        return configuration.getStringValue(getFullKey(name), defaultValue);
    }

    public int getIntValue(String name, int defaultValue) {
        return configuration.getIntValue(getFullKey(name), defaultValue);
    }

    public long getLongValue(String name, long defaultValue) {
        return configuration.getLongValue(getFullKey(name), defaultValue);
    }

    public float getFloatValue(String name, float defaultValue) {
        return configuration.getFloatValue(getFullKey(name), defaultValue);
    }

    public boolean getBooleanValue(String name, boolean defaultValue) {
        return configuration.getBooleanValue(getFullKey(name), defaultValue);
    }

    public <T> T getBeanValue(String configKey, Class<T> clazz, T defaultValue) {
        return getBeanValue(configKey, clazz, defaultValue, false);
    }

    public <T> T getBeanValue(String configKey, Class<T> clazz, T defaultValue, boolean isEncoded) {
        String ret = configuration.getStringValue(getFullKey(configKey), null);
        if (StringUtils.isNullOrEmpty(ret)) {
            return defaultValue;
        } else {
            if (isEncoded) {
                try {
                    return SerializerUtils.read(URLDecoder.decode(ret, "UTF-8"), clazz);
                } catch (IOException e) {
                    logger.warn("fail to parse from configManager, value: " + ret, e);
                    return defaultValue;
                }
            } else {
                try {
                    return SerializerUtils.read(ret, clazz);
                } catch (IOException e) {
                    logger.warn("fail to parse from configManager, value: " + ret, e);
                    return defaultValue;
                }
            }
        }
    }

    public void addPropertiesChangedListener(String name, ConfigChangedListener listener) {
        configuration.addListener(getFullKey(name), listener);
    }

    /**
     * full key
     *
     * @param name
     * @return
     */
    private String getFullKey(String name) {
        if(rhinoType == null){
            return name;
        }

        StringBuilder builder = new StringBuilder();
        builder.append(appKey);
        builder.append(".");
        builder.append(rhinoType.getTag());
        builder.append(".");
        builder.append(rhinoKey);
        builder.append(".");
        builder.append(name);
        return builder.toString();
    }

    private Map<Field, Method> methodMap = new HashMap<>();

    /**
     * 属性序列化
     *
     * @return
     */
    public String toJson() {
        Field[] fields = this.getClass().getDeclaredFields();
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        for (Field field : fields) {
            JsonIgnore jsonIgnore = field.getAnnotation(JsonIgnore.class);
            String name = field.getName();
            Object value = null;
            if (Modifier.isStatic(field.getModifiers()) || jsonIgnore != null) {
                continue;
            }
            Method method = methodMap.get(field);
            if (method == null) {
                char[] cs = name.toCharArray();
                cs[0] -= 32;
                try {
                    method = this.getClass().getDeclaredMethod("get" + String.valueOf(cs));
                    method.setAccessible(true);
                    methodMap.put(field, method);
                } catch (NoSuchMethodException e) {
                    logger.error("NoSuchMethodException", e);
                }
            }

            if (method == null) {
                continue;
            }
            try {
                value = method.invoke(this);
            } catch (IllegalAccessException e) {
                logger.error("IllegalAccessException", e);
            } catch (InvocationTargetException e) {
                logger.error("InvocationTargetException", e);
            }

            if (value != null) {
                builder.append("\"");
                builder.append(name);
                builder.append("\"");
                builder.append(":");
                builder.append("\"");
                builder.append(value.toString());
                builder.append("\"");
                builder.append(",");
            }
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append("}");
        return builder.toString();
    }

}
