
package src.rhino.fault;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.mysql.cj.util.StringUtils;

import src.rhino.RhinoConfigProperties;
import src.rhino.RhinoType;
import src.rhino.annotation.FaultInject;
import src.rhino.config.ConfigChangedListener;
import src.rhino.config.Configuration;
import src.rhino.dispatcher.RhinoEvent;
import src.rhino.dispatcher.RhinoEventDispatcher;
import src.rhino.util.AppUtils;
import src.rhino.util.MtraceUtils;
import src.rhino.util.SerializerUtils;

/**
 * @author zhanjun on 2017/4/25.
 */
public class DefaultFaultInjectProperties extends RhinoConfigProperties implements FaultInjectProperties {

    private static String localIp = AppUtils.getLocalIp();
    private static ScheduledExecutorService delayExecutor;
    private RhinoEventDispatcher faultInjectEventDispatcher;

    private volatile boolean enable;
    private volatile FaultBean faultBean;
    private Future startFuture;
    private Future stopFuture;

    static {
        delayExecutor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "rhino-delay-task");
                thread.setDaemon(true);
                return thread;
            }
        });
    }

    public DefaultFaultInjectProperties(String rhinoKey) {
        this(AppUtils.getAppName(), rhinoKey, null);
    }

    public DefaultFaultInjectProperties(FaultInject fault) {
        this(AppUtils.getAppName(), fault.rhinoKey(), null);
    }

    public DefaultFaultInjectProperties(String appKey, String rhinoKey, Configuration configuration) {
        super(appKey, rhinoKey, RhinoType.FaultInject, configuration);
        this.faultBean = getBeanValue(configKeySuffix, FaultBean.class, new FaultBean(), true);
        this.faultInjectEventDispatcher = new FaultInjectEventDispatcher(rhinoKey);
    }

    @Override
    public boolean getIsActive() {
        return enable && checkMtrace();
    }

    @Override
    public boolean getIsActive(FaultInjectContext context) {
        if (context == null) {
            return getIsActive();
        }
        return getIsActive() && checkContext(context);
    }

    @Override
    public <T> T getMockValue(Class<T> returnType) throws IOException {
        String mockValue = faultBean.getMockValue();
        if (StringUtils.isNullOrEmpty(mockValue)) {
            return null;
        }
        return SerializerUtils.read(mockValue, returnType);
    }

    @Override
    public String getExceptionType() {
        return faultBean.getExceptionType();
    }

    @Override
    public int getType() {
        return faultBean.getType();
    }

    @Override
    public int getMaxDelay() {
        return faultBean.getMaxDelay();
    }

    @Override
    public float getSampleRate() {
        return faultBean.getSampleRate();
    }

    @Override
    public boolean getIsRandomDelay() {
        return faultBean.getRandomDelay();
    }

    private Map<String, String> getSingleFeatures() {
        return faultBean.getSingleValueFeatures();
    }

    private Map<String, Set<String>> getMultiFeatures() {
        return faultBean.getMultiValueFeatures();
    }

    @Override
    public void addConfigChangedListener(ConfigChangedListener listener) {
        addPropertiesChangedListener(configKeySuffix, new ConfigChangedListener() {

            private final Object lock = new Object();

            @Override
            public void invoke(String key, String oldValue, String newValue) {
                synchronized (lock) {
                    FaultBean prevFaultBean = faultBean;
                    faultBean = getBeanValue(configKeySuffix, FaultBean.class, prevFaultBean, true);
                    logger.info("fault config changed. Prev value: " + prevFaultBean + ". Current value: " + faultBean);
                    // reset status
                    reset();
                    if (faultBean.getIps() != null && faultBean.getIps().contains(localIp)) {
                        handle(faultBean);
                    }
                    faultInjectEventDispatcher.dispatchEvent(new RhinoEvent(FaultInjectEventType.CONFIG_CHANGE));
                    logger.info("Current status:" + enable);
                }
            }
        });
    }

    private void reset() {
        if (startFuture != null) {
            startFuture.cancel(false);
            startFuture = null;
        }

        if (stopFuture != null) {
            stopFuture.cancel(false);
            stopFuture = null;
        }
        enable = false;
    }

    /**
     * @param currentBean
     */
    private void handle(FaultBean currentBean) {
        FaultBeanAction action = currentBean.getAction();
        long startTime = currentBean.getStartTime().getTime();
        long stopTime = currentBean.getEndTime().getTime();
        long now = System.currentTimeMillis();

        if (FaultBeanAction.INITIAL.equals(action)) {
            if (startTime >= stopTime || stopTime <= now) {
                return;
            }
            long startDelay = startTime - now;
            if (startDelay > 0) {
                dispatchEvent(FaultBeanAction.INITIAL);
                startFuture = createDelayTask(startDelay, true, FaultBeanAction.START);
            } else {
                dispatchEvent(FaultBeanAction.START);
                enable = true;
            }

            stopFuture = createDelayTask(stopTime - now, false, FaultBeanAction.STOP);
        }

        // 开始故障
        if (FaultBeanAction.START.equals(action)) {
            dispatchEvent(action);
            enable = true;
            long stopDelay = stopTime - now;
            if (stopDelay > 0) {
                stopFuture = createDelayTask(stopDelay, false, FaultBeanAction.STOP);
            } else {
                dispatchEvent(FaultBeanAction.STOP);
                enable = false;
            }
        }

        // 取消故障
        if (FaultBeanAction.CANCEL.equals(action)) {
            // do nothing
            dispatchEvent(action);
        }

        // 中止故障
        if (FaultBeanAction.STOP.equals(action)) {
            dispatchEvent(action);
        }
    }

    /**
     * @param delta
     * @param isStart
     * @return
     */
    private Future createDelayTask(long delta, final boolean isStart, final FaultBeanAction action) {
        Future future = delayExecutor.schedule(new Runnable() {
            @Override
            public void run() {
                dispatchEvent(action);
                enable = isStart;
            }
        }, delta, TimeUnit.MILLISECONDS);

        return future;
    }

    /**
     * @param action
     */
    private void dispatchEvent(FaultBeanAction action) {
        FaultInjectEventType eventType = action.getEventType();
        faultInjectEventDispatcher.dispatchEvent(new RhinoEvent(eventType, new FaultInjectEventData(faultBean.getId())));
    }

    /**
     * 检查mtrace是否被tag 只有染色流量可注入
     *
     * @return
     */
    private boolean checkMtrace() {
        if (faultBean.getDyeEnabled()) {
            return MtraceUtils.isTest();
        }
        // 染色开关关闭，默认都可注入
        return true;
    }

    /**
     * @param context
     * @return
     */
    private boolean checkContext(FaultInjectContext context) {
        if (getSingleFeatures() != null) {
            for (Map.Entry<String, String> entry : getSingleFeatures().entrySet()) {
                String key = entry.getKey();
                String expectedValue = entry.getValue();
                String actualValue = context.get(key);
                if (!expectedValue.equals(actualValue)) {
                    return false;
                }
            }
        }

        if (getMultiFeatures() != null) {
            for (Map.Entry<String, Set<String>> entry : getMultiFeatures().entrySet()) {
                String key = entry.getKey();
                Set<String> expectedValue = entry.getValue();
                String actualValue = context.get(key);
                if (!expectedValue.contains(actualValue)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String toJson() {
        try {
            return "{\"enable\":" + enable + ",\"faultBean\":" + SerializerUtils.write(faultBean) + "}";
        } catch (Exception e) {
            logger.error("DefaultFaultInjectProperties to json failed", e);
        }
        return "";
    }
}
