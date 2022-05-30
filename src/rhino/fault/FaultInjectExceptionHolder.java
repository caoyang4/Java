package src.rhino.fault;

import java.util.concurrent.ConcurrentHashMap;

import src.rhino.exception.RhinoIgnoreException;
import src.rhino.log.Logger;
import src.rhino.log.LoggerFactory;

/**
 * Created by zhanjun on 2017/6/2.
 */
public class FaultInjectExceptionHolder {

    private static final Logger logger = LoggerFactory.getLogger(FaultInjectExceptionHolder.class);
    private static ConcurrentHashMap<String, Class<? extends Exception>> exceptionClassHolder = new ConcurrentHashMap<>();

    /**
     *
     * @param exceptionType
     * @return
     * @throws ClassNotFoundException
     */
    public static Class<? extends Exception> getExceptionClass(String exceptionType) {
        Class<? extends Exception> exceptionClass = exceptionClassHolder.get(exceptionType);
        if (exceptionClass != null) {
            return exceptionClass;
        }

        try {
            exceptionClass = (Class<? extends Exception>)Class.forName(exceptionType);
        } catch (ClassNotFoundException e) {
            // 如果异常类不存在，则使用RhinoIgnoreException替代
            exceptionClass = RhinoIgnoreException.class;
        }

        Class<? extends Exception> exceptionClass0 = exceptionClassHolder.putIfAbsent(exceptionType, exceptionClass);
        if (exceptionClass0 != null) {
            return exceptionClass0;
        }
        return exceptionClass;
    }
}
