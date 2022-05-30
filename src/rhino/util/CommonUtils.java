package src.rhino.util;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;

import src.rhino.RhinoConfigProperties;
import src.rhino.exception.RhinoTimeoutException;

/**
 * @author zhanjun on 2017/09/25.
 */
public final class CommonUtils {

    /**
     * 获取真实异常
     *
     * @param throwable
     * @return
     */
    public static Exception getActualException(Throwable throwable) {
        if (throwable instanceof ExecutionException) {
            throwable = throwable.getCause();
        }
        if (throwable instanceof InvocationTargetException) {
            throwable = ((InvocationTargetException) throwable).getTargetException();
        }
        if (throwable instanceof UndeclaredThrowableException) {
            throwable = ((UndeclaredThrowableException) throwable).getUndeclaredThrowable();
        }
        if (throwable instanceof InterruptedException) {
            throwable = new RhinoTimeoutException();
        }
        if (throwable instanceof Exception) {
            return (Exception) throwable;
        }
        return new Exception(throwable);
    }

    /**
     *
     * @param properties
     * @return
     */
    public static String parseProperties(RhinoConfigProperties properties) {
        try {
            return URLEncoder.encode(properties.toJson(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // ignore
        }
        return null;
    }

    /**
     * 参数校验
     * @param condition
     * @param message
     */
    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    public static long currentMillis(){
        return System.currentTimeMillis();
    }
}
