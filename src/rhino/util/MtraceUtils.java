
package src.rhino.util;

import java.lang.reflect.Method;

/**
 * Created by wanghao on 17/9/26.
 */
public class MtraceUtils {

    private static Method isTestMethod = null;
    private static Method serverRecv = null;
    private static Method setTest = null;
    private static Method serverSend = null;
    public static final String TEST_FLAG = "$Test";

    static {
        Class<?> mtraceTracerClazz = null;
        try {
            mtraceTracerClazz = Class.forName("com.meituan.mtrace.Tracer");
        } catch (Throwable ignore) {
            //ignore exception
        }

        if (mtraceTracerClazz != null) {
            try {
                isTestMethod = mtraceTracerClazz.getDeclaredMethod("isTest");
                isTestMethod.setAccessible(true);
            } catch (NoSuchMethodException e) {
                //ignore exception
            }
            try {
                serverRecv = mtraceTracerClazz.getDeclaredMethod("serverRecv", String.class);
                serverRecv.setAccessible(true);
            } catch (NoSuchMethodException e) {
                //ignore exception
            }

            try {
                setTest = mtraceTracerClazz.getDeclaredMethod("setTest", boolean.class);
                setTest.setAccessible(true);
            } catch (NoSuchMethodException e) {
                //ignore exception
            }
            try {
                serverSend = mtraceTracerClazz.getDeclaredMethod("serverSend");
                serverSend.setAccessible(true);
            } catch (NoSuchMethodException e) {
                //ignore exception
            }
        }
    }

    /**
     * @return
     */
    public static boolean isTest() {
        if (isTestMethod != null) {
            try {
                return (Boolean) isTestMethod.invoke(null);
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
    }

    public static void serverRecv() {
        if (serverRecv != null) {
            try {
                serverRecv.invoke(null, "test");
            } catch (Exception e) {
                //ignore exception
            }
        }
    }

    public static void setTest(boolean isTest) {
        if (setTest != null) {
            try {
                setTest.invoke(null, isTest);
            } catch (Exception e) {
                //ignore exception
            }
        }
    }

    public static void serverSend() {
        if (serverSend != null) {
            try {
                serverSend.invoke(null);
            } catch (Exception e) {
                //ignore exception
            }
        }
    }

    /**
     * @param name
     * @return
     */
    public static String addTestFlag(String name) {
        return name + TEST_FLAG;
    }
}
