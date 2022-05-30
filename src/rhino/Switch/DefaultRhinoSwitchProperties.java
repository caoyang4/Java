package src.rhino.Switch;

import com.mysql.cj.util.StringUtils;

import src.cat.Cat;
import src.cat.message.Event;
import src.cat.message.Transaction;
import src.rhino.RhinoConfigProperties;
import src.rhino.RhinoType;
import src.rhino.config.ConfigChangedListener;
import src.rhino.util.SerializerUtils;

/**
 * Created by zhen on 2019/1/10.
 */
public class DefaultRhinoSwitchProperties extends RhinoConfigProperties implements RhinoSwitchProperties {

    private static final String TRANSACTION_TYPE = "Rhino.Switch.PropertyChange";

    private String fullKey;
    private volatile RhinoSwitchBean rhinoSwitchBean;

    public DefaultRhinoSwitchProperties(String appKey, String key) {
        super(appKey, key, RhinoType.RhinoSwitch, null);
        this.fullKey = appKey + "." + key;
        this.rhinoSwitchBean = parse(getStringValue(configKeySuffix, null));
    }

    @Override
    public void addConfigChangedListener(ConfigChangedListener listener) {
        addPropertiesChangedListener(configKeySuffix, new ConfigChangedListener() {
            private Object lock = new Object();

            @Override
            public void invoke(String key, String oldValue, String newValue) {
                synchronized (lock) {
                    Transaction t = Cat.newTransaction(TRANSACTION_TYPE, rhinoKey);
                    try {
                        Cat.logEvent(TRANSACTION_TYPE, rhinoKey, Event.SUCCESS, "oldValue=" + oldValue + "&newValue=" + newValue);
                        if (StringUtils.equals(oldValue, newValue)) {
                            return;
                        }
                        rhinoSwitchBean = parse(newValue);
                        t.setStatus(Transaction.SUCCESS);
                    } finally {
                        t.complete();
                    }
                }
            }
        });
    }


    @Override
    public boolean isActive() {
        return rhinoSwitchBean.isActive() && rhinoSwitchBean.match();
    }

    @Override
    public String getFullKey() {
        return fullKey;
    }

    @Override
    public String getString(String defaultValue) {
        if (isActive()) {
            String grayValue = getGrayValue();
            if (grayValue != null) {
                return grayValue;
            }
        }
        String rowKeyValue = configuration.getStringValue(rhinoKey, defaultValue);
        if (!defaultValue.equals(rowKeyValue)) {
            return rowKeyValue;
        }
        return configuration.getStringValue(fullKey, defaultValue);
    }

    @Override
    public int getInt(int defaultValue) {
        if (isActive()) {
            try {
                return Integer.parseInt(getGrayValue());
            } catch (Exception e) {
                //ignore
            }
        }
        int rowKeyValue = configuration.getIntValue(rhinoKey, defaultValue);
        if (defaultValue != rowKeyValue) {
            return rowKeyValue;
        }
        return configuration.getIntValue(fullKey, defaultValue);
    }

    @Override
    public long getLong(long defaultValue) {
        if (isActive()) {
            try {
                return Long.parseLong(getGrayValue());
            } catch (Exception e) {
                //ignore
            }
        }
        long rowKeyValue = configuration.getLongValue(rhinoKey, defaultValue);
        if (defaultValue != rowKeyValue) {
            return rowKeyValue;
        }
        return configuration.getLongValue(fullKey, defaultValue);
    }

    @Override
    public boolean getBoolean(boolean defaultValue) {
        if (isActive()) {
            return Boolean.parseBoolean(getGrayValue());
        }
        boolean rowKeyValue = configuration.getBooleanValue(rhinoKey, defaultValue);
        if (defaultValue != rowKeyValue) {
            return rowKeyValue;
        }
        return configuration.getBooleanValue(fullKey, defaultValue);
    }

    @Override
    public String getConfigName() {
        return configuration.getName();
    }

    @Override
    public String toJson() {
        return "{\"fullKey\":\"" + fullKey + "\"" +
                ",\"rhinoSwitchBean\":" + rhinoSwitchBean.toJson() + "}";
    }

    private String getGrayValue() {
        return rhinoSwitchBean.getGrayValue();
    }

    private RhinoSwitchBean parse(String value) {
        if (StringUtils.isNullOrEmpty(value)) {
            try {
                RhinoSwitchBean rhinoSwitchBean = SerializerUtils.read(value, RhinoSwitchBean.class);
                rhinoSwitchBean.createMatcher();
                return rhinoSwitchBean;
            } catch (Exception e) {
                logger.warn("fail to parse from configManager, value: " + value, e);
            }
        }
        return new RhinoSwitchBean(false);
    }


}
