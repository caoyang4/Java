
package src.rhino.fault;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * @author wanghao on 17/10/30.
 */
public class FaultInjectContext {

    private static final String KEY_TARGET_ADDRESS = "targetAddress";
    private static final String KEY_TARGET_IDC = "targetIdc";

    private Map<String, String> features;
    private boolean isDelay;
    private int delayTime;

    private FaultInjectContext() {

    }

    public static FaultInjectContext create() {
        return new FaultInjectContext();
    }

    public FaultInjectContext put(String key, String value) {
        if (features == null) {
            features = new HashMap<>();
        }
        if (KEY_TARGET_ADDRESS.equals(key)) {
            if (value != null) {
                features.put(KEY_TARGET_IDC, value);
            }
        }
        if (value != null) {
            features.put(key, value);
        }
        return this;
    }

    public String get(String key) {
        return features.get(key);
    }

    public Set<Map.Entry<String, String>> entries() {
        return features.entrySet();
    }

    public boolean isEmpty() {
        return features.isEmpty();
    }

    public boolean isDelay() {
        return isDelay;
    }

    public int getDelayTime() {
        return delayTime;
    }

    public void setDelay(boolean delay) {
        isDelay = delay;
    }

    public void setDelayTime(int delayTime) {
        this.delayTime = delayTime;
    }

    @Override
    public String toString() {
        if (features.size() == 0) {
            return "";
        }

        StringBuilder builder = new StringBuilder(128);
        builder.append("{");
        for (String key : features.keySet()) {
            builder.append(key);
            builder.append("=");
            builder.append(features.get(key));
            builder.append(",");
        }
        if (builder.length() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        builder.append("}");
        return builder.toString();
    }
}
