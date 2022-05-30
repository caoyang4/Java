package src.rhino.limit.feature;

import java.util.HashMap;
import java.util.Map;

import com.mysql.cj.util.StringUtils;


/**
 * @author zhanjun on 2017/8/13.
 * 保存业务的特征数据
 */
public class Features {

    private Map<String, String> holder = new HashMap<>();

    private Features() {}

    public static Features create() {
        return new Features();
    }

    public Features put(String featureName, String value) {
        holder.put(featureName, value);
        return this;
    }

    public String get(String featureName) {
        return holder.get(featureName);
    }

    public boolean isEmpty() {
        return holder.isEmpty();
    }

    @Override
    public String toString() {
        if (holder.size() == 0) {
            return "";
        }

        StringBuilder builder = new StringBuilder(128);
        builder.append("{");
        for (String key : holder.keySet()) {
            builder.append(key);
            builder.append("=");
            builder.append(holder.get(key));
            builder.append(",");
        }
        if (builder.length() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        builder.append("}");
        return builder.toString();
    }
}
