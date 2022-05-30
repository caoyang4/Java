package src.rhino.fault.type;

import java.lang.reflect.Constructor;

import src.rhino.exception.RhinoIgnoreException;
import src.rhino.fault.FaultInjectEventType;
import src.rhino.fault.FaultInjectExceptionHolder;
import src.rhino.fault.FaultInjectProperties;
import src.rhino.util.ClassUtils;

/**
 * @author zhanjun on 2017/6/30.
 */
public class ExceptionFaultSimulator extends BasicFaultSimulator {

    private Exception cachedException;

    public ExceptionFaultSimulator(FaultInjectProperties requestInjectProperties) {
        super(requestInjectProperties, FaultInjectEventType.EXCEPTION);
        initCachedException();
    }

    @Override
    public <T> T doSimulate(Class<T> returnType) throws Exception {
        if (!cachedException.getClass().getName().equals(requestInjectProperties.getExceptionType())) {
            initCachedException();
        }
        throw cachedException;
    }

    private void initCachedException() {
        String exceptionType = requestInjectProperties.getExceptionType();
        try {
            Class<? extends Exception> exceptionClass = FaultInjectExceptionHolder.getExceptionClass(exceptionType);
            Constructor[] constructors = exceptionClass.getDeclaredConstructors();
            Constructor constructor = constructors[constructors.length - 1];
            constructor.setAccessible(true);
            Class[] paramClasses = constructor.getParameterTypes();
            Object[] args = new Object[paramClasses.length];
            for (int i = 0; i < paramClasses.length; i++) {
                Class clazz = paramClasses[i];
                if (clazz.isPrimitive()) {
                    args[i] = ClassUtils.getActualPrimitiveTypeDefaultValue(clazz);
                } else {
                    args[i] = null;
                }
            }
            cachedException = (Exception) constructor.newInstance(args);
        } catch (Exception e) {
            cachedException = new RhinoIgnoreException("fail to new instance for exception " + exceptionType + e.getMessage());
        }
    }
}
