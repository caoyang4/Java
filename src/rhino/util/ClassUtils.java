package src.rhino.util;

/**
 * Created by zhanjun on 2017/7/7.
 */
public class ClassUtils {

    public static Class loadClass(ClassLoader classLoader, String className) throws ClassNotFoundException {
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        return org.apache.commons.lang.ClassUtils.getClass(classLoader, className);
    }

    public static Class loadClass(String className) throws ClassNotFoundException {
        return loadClass(null, className);
    }

    public static ClassLoader getCurrentClassLoader(ClassLoader classLoader) {
        if (classLoader != null) {
            return classLoader;
        }
        ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
        if (currentLoader != null) {
            return currentLoader;
        }
        return ClassUtils.class.getClassLoader();
    }

    public static Object getActualPrimitiveTypeDefaultValue(Class<?> clazz) {
        switch (clazz.getName()) {
            case "byte":
                return (byte) 0;
            case "short":
                return (short) 0;
            case "long":
                return (long) 0;
            case "boolean":
                return false;
            case "float":
                return 0.0f;
            case "double":
                return 0.0d;
            case "char":
                return 'a';
            default:
                return 0;
        }
    }
}