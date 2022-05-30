package src.rhino.circuit.degrade;

import java.lang.reflect.Constructor;

import src.rhino.circuit.CircuitBreakerProperties;
import src.rhino.exception.RhinoRuntimeException;
import src.rhino.util.ClassUtils;

/**
 * Created by zhanjun on 2018/3/29.
 */
public class ExceptionDegradeStrategy extends AbstractDegradeStrategy {

    private Exception cachedException;

    public ExceptionDegradeStrategy(CircuitBreakerProperties properties) {
        super(properties);
        this.cachedException = parse(this.value);

    }

    private Exception parse(String value) {
        Exception target;
        try {
            // 测试异常是否能实例化
            Class<?> exceptionType = Class.forName(value);
            Constructor[] constructors = exceptionType.getConstructors();
            Constructor constructor = constructors[constructors.length - 1];
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            Object[] parameters = new Object[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> parameterType = parameterTypes[i];
                if (parameterType.isPrimitive()) {
                    parameters[i] = ClassUtils.getActualPrimitiveTypeDefaultValue(parameterType);
                } else {
                    parameters[i] = null;
                }
            }
            target = (Exception) constructor.newInstance(parameters);
        } catch (Exception e) {
            String msg = "降级策略[指定异常]配置有误，当前配置异常类型为：" + value;
            target = new RhinoRuntimeException(msg, e);
        }
        return target;
    }

    @Override
    public Object degrade() throws Exception {
        throw cachedException;
    }
}
