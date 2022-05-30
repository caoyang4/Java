package src.rhino.circuit.degrade;

import java.io.IOException;


import com.mysql.cj.util.StringUtils;
import src.rhino.circuit.CircuitBreakerProperties;
import src.rhino.util.SerializerUtils;

/**
 * Created by zhanjun on 2018/3/29.
 */
public class ConstantDegradeStrategy extends AbstractDegradeStrategy {

    private static final String SEPARATOR = "###";
    private Class clazz;
    private String jsonValue;

    public ConstantDegradeStrategy(CircuitBreakerProperties properties) {
        super(properties);
        parse(value);
    }

    /**
     * 验证配置是否有误
     * @param value
     */
    private void parse(String value) {

        // 如果配置的字符串为空、或者没有###分隔符，则直接抛异常
        if (StringUtils.isNullOrEmpty(value) || value.indexOf(SEPARATOR) <= 0) {
            String msg = "降级策略[返回常量值]配置有误，当前配置：" + value;
            throw new IllegalArgumentException(msg);
        }
        String[] valueArray = value.split(SEPARATOR);
        String clazzName = valueArray[0];
        if (valueArray.length > 1) {
            this.jsonValue = valueArray[1];
        }

        try {
            this.clazz = Class.forName(clazzName);
            if (jsonValue != null && clazz != String.class) {
                SerializerUtils.read(jsonValue, clazz);
            }
        } catch (Exception e) {
            String msg = "降级策略[返回常量值]配置有误，当前配置的常量类型为：" + valueArray[0] + "，值为：" + valueArray[1];
            throw new IllegalArgumentException(msg, e);
        }
    }

    @Override
    public Object degrade() {
        if (jsonValue == null) {
            return null;
        }

        if (clazz == String.class) {
            return jsonValue;
        }

        try {
            return SerializerUtils.read(jsonValue, clazz);
        } catch (IOException e) {
            // ignore exception
        }
        return null;
    }
}
