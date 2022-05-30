package src.rhino.log;

import java.lang.reflect.Constructor;

import com.mysql.cj.util.StringUtils;

import src.rhino.util.AppUtils;

public class LoggerFactory {

    private static Constructor logConstructor;

    private static final String LOG_ROOT_KEY = "rhino.log.dir";
    private static final String LOG_ROOT_DEFAULT = "/data/applogs/rhino";

    public static String LOG_ROOT;

    static {
        if (StringUtils.isNullOrEmpty(System.getProperty(LOG_ROOT_KEY))) {
            System.setProperty(LOG_ROOT_KEY, LOG_ROOT_DEFAULT);
        }
        String appName = AppUtils.getAppName();
        if (appName != null) {
            System.setProperty("app.name", appName);
        }
        LOG_ROOT = System.getProperty(LOG_ROOT_KEY);

        // slf4j > log4j2 > log4j > simple > null
        if(xmdlogExists()) {
            tryImplementation("org.slf4j.Logger", "src.rhino.log.Slf4jLogger");
        }
        tryImplementation("org.apache.logging.log4j.Logger", "src.rhino.log.Log4j2Logger");
        tryImplementation("org.apache.log4j.Logger", "src.rhino.client.log.Log4jLogger");


        if (logConstructor == null) {
            try {
                logConstructor = SimpleLogger.class.getConstructor(String.class);
            } catch (Exception e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    private static boolean xmdlogExists() {
        try {
            Resources.classForName("com.meituan.inf.xmdlog.config.XMDConfiguration");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private static void tryImplementation(String testClassName, String implClassName) {
        if (logConstructor != null) {
            return;
        }

        try {
            if(testClassName != null) {
                Resources.classForName(testClassName);
            }
            Class implClass = Resources.classForName(implClassName);
            logConstructor = implClass.getConstructor(new Class[] {String.class});

            Class<?> declareClass = logConstructor.getDeclaringClass();
            if (!Logger.class.isAssignableFrom(declareClass)) {
                logConstructor = null;
            }

            try {
                if (null != logConstructor) {
                    logConstructor.newInstance(LoggerFactory.class.getName());
                }
            } catch (Throwable t) {
                logConstructor = null;
            }

        } catch (Throwable t) {
//            ignore
        }
    }

    public static Logger getLogger(Class clazz) {
        return getLogger(clazz.getName());
    }

    public static Logger getLogger(String loggerName) {
        try {
            return (Logger) logConstructor.newInstance(loggerName);
        } catch (Throwable t) {
            throw new RuntimeException("failed to create logger for " + loggerName + ", cause: " + t, t);
        }
    }
}
