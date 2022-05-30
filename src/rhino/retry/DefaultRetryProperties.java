package src.rhino.retry;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.mysql.cj.util.StringUtils;
import org.springframework.util.CollectionUtils;

import src.rhino.RhinoConfigProperties;
import src.rhino.RhinoType;
import src.rhino.config.ConfigChangedListener;
import src.rhino.config.PropertyChangedListener;
import src.rhino.util.AppUtils;
import src.rhino.util.Preconditions;
//import src.rhino.util.SerializerUtils;

/**
 * Created by zhen on 2019/2/20.
 */
public class DefaultRetryProperties extends RhinoConfigProperties implements RetryProperties {

    private volatile RetryPropertiesBean retryPropertiesBean;
    /**
     * retryForExceptions （true） 和 ignoredException （false） 的状态集合
     */
    private volatile ConcurrentHashMap<Class<? extends Throwable>, Boolean> exceptionStateMap = new ConcurrentHashMap<>();

    public DefaultRetryProperties(String rhinoKey, DefaultRetryProperties.Setter setter) {
        super(AppUtils.getAppName(), rhinoKey, RhinoType.RetryPolicy, null);
        String value = getStringValue(configKeySuffix, "");
        //配置中心配置
        RetryPropertiesBean retryPropertiesBean = parseRetryPropertiesData(value);
        if (retryPropertiesBean == null) {
            if (setter == null) {
                setter = new Setter();
            }
            retryPropertiesBean = new RetryPropertiesBean(setter);
        }
        this.retryPropertiesBean = retryPropertiesBean;
        exceptionStateMap.clear();
        addConfigChangedListener(null);
    }

    @Override
    public void addConfigChangedListener(ConfigChangedListener listener) {
        addPropertiesChangedListener(configKeySuffix, new ConfigChangedListener() {

            private Object lock = new Object();

            @Override
            public void invoke(String key, String oldValue, String newValue) {
                synchronized (lock) {
                    if (StringUtils.isNullOrEmpty(newValue) || newValue.equals(oldValue)) {
                        return;
                    }
                    RetryPropertiesBean newOne = parseRetryPropertiesData(newValue);
                    if (newOne == null) {
                        return;
                    }
                    RetryPropertiesBean oldONe = retryPropertiesBean;
                    retryPropertiesBean = newOne;
                    exceptionStateMap.clear();
                    for (PropertyChangedListener listener : propertyChangedListeners) {
                        listener.trigger(oldONe, newOne);
                    }
                }
            }
        });

    }

    @Override
    public boolean isActive() {
        return retryPropertiesBean.isActive();
    }

    @Override
    public boolean canRetry(RetryContext retryContext) {
        return retryOnException(retryContext)
                && !overMaxAttempt(retryContext)
                && !overMaxDuration(retryContext);
    }

    @Override
    public int getDelayStrategy() {
        return retryPropertiesBean.getDelayStrategy();
    }

    @Override
    public long getDelay() {
        return retryPropertiesBean.getDelay();
    }

    @Override
    public long getMinDelay() {
        return retryPropertiesBean.getMinDelay();
    }

    @Override
    public long getMaxDelay() {
        return retryPropertiesBean.getMaxDelay();
    }

    @Override
    public int getMaxAttempts() {
        return retryPropertiesBean.getMaxAttempts();
    }

    @Override
    public long getMaxDuration() {
        return retryPropertiesBean.getMaxDuration();
    }

    @Override
    public double getMultiplier() {
        return retryPropertiesBean.getMultiplier();
    }

    /**
     * exception是否为可以重试的target exception
     *
     * @param retryContext
     * @return
     */
    private boolean retryOnException(RetryContext retryContext) {
        Throwable t = retryContext.getLastThrowable();
        //初次重试，返回true
        if (t == null) {
            return true;
        }
        Class<? extends Throwable> eClass = t.getClass();
        if (exceptionStateMap.containsKey(eClass)) {
            return exceptionStateMap.get(eClass);
        }
        //throwable 必须是不能ignore && 必须是需要retry的
        boolean result = !isIgnoredException(eClass) && isRetryForException(eClass);
        exceptionStateMap.put(eClass, result);
        return result;
    }

    private boolean isIgnoredException(Class<? extends Throwable> eClass) {
        Set<Class<? extends Throwable>> ignoredExceptions = retryPropertiesBean.getIgnoredExceptions();
        //ignoredExceptions为空，返回false
        return !CollectionUtils.isEmpty(ignoredExceptions) && isTargetClass(eClass, ignoredExceptions);
    }

    private boolean isRetryForException(Class<? extends Throwable> eClass) {
        Set<Class<? extends Throwable>> retryForExceptions = retryPropertiesBean.getRetryForExceptions();
        //retryForExceptions 为空，默认所有exception都要retry
        return CollectionUtils.isEmpty(retryForExceptions) || isTargetClass(eClass, retryForExceptions);
    }

    /**
     * target 是否为 classOptions 的子类
     *
     * @param target
     * @param classOptions
     * @return
     */
    private boolean isTargetClass(Class<? extends Throwable> target, Set<Class<? extends Throwable>> classOptions) {
        for (Class<?> classOption : classOptions) {
            if (classOption.isAssignableFrom(target)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 最大重试次数检测
     *
     * @param retryContext
     * @return
     */
    private boolean overMaxAttempt(RetryContext retryContext) {
        return retryContext.getAttempt() > getMaxAttempts();
    }

    /**
     * 超时检测
     *
     * @param retryContext
     * @return
     */
    private boolean overMaxDuration(RetryContext retryContext) {
        //maxDuration<=0 不做超时检查
        return getMaxDuration() > 0 && System.currentTimeMillis() - retryContext.getRetryStartTimestamp() >= getMaxDuration();
    }


    private RetryPropertiesBean parseRetryPropertiesData(String value) {
        if (!StringUtils.isNullOrEmpty(value)) {
            try {
//                return SerializerUtils.read(value, RetryPropertiesBean.class);
                return null;
            } catch (Exception e) {
                logger.warn("fail to parse from configManager, value: " + value, e);
            }
        }
        return null;
    }

    @Override
    public String toJson() {
        return "{\"" +
                configKeySuffix +
                "\":" +
//                    SerializerUtils.write(retryPropertiesBean) +
                "}";
    }

    public static final class Setter {
        /**
         * 开关
         */
        private boolean active = RetryProperties.ACTIVE;
        /**
         * 重试次数
         */
        private int maxAttempts = RetryProperties.MAX_ATTEMPTS;
        /**
         * 延迟时间计算类型
         */
        private int delayStrategy = RetryProperties.DELAY_STRATEGY;
        /**
         * 延迟时间，单位 millisecond
         */
        private long delay = RetryProperties.DELAY;

        private long minDelay = RetryProperties.MIN_DELAY;

        private long maxDelay = RetryProperties.MAX_DELAY;

        /**
         * 最大持续时间，单位 millisecond
         */
        private long maxDuration = RetryProperties.MAX_DURATION;

        private double multiplier = RetryProperties.MULTIPLIER;

        private Set<Class<? extends Throwable>> retryForExceptions;

        private Set<Class<? extends Throwable>> ignoredExceptions;

        public Setter() {
        }

        public Setter(src.rhino.annotation.Retry retry) {
            this.active = retry.isActive();
            this.maxAttempts = retry.maxAttempts();
            this.delayStrategy = retry.delayStrategy();
            this.delay = retry.delay();
            this.minDelay = retry.minDelay();
            this.maxDelay = retry.maxDelay();
            this.maxDuration = retry.maxDuration();
            this.multiplier = retry.multiplier();
            if (retry.ignoreExceptions().length > 0) {
                this.ignoredExceptions = new HashSet<>(Arrays.asList(retry.ignoreExceptions()));
            }
            if (retry.retryOnExceptions().length > 0) {
                this.retryForExceptions = new HashSet<>(Arrays.asList(retry.retryOnExceptions()));
            }
        }

        public Setter withActive(boolean active) {
            this.active = active;
            return this;
        }

        public Setter withMaxAttempts(int maxAttempts) {
            Preconditions.checkArgument(maxAttempts >= 0, "illegal maxAttempts:" + maxAttempts);
            this.maxAttempts = maxAttempts;
            return this;
        }

        public Setter withDelayStrategy(int delayStrategy) {
            Preconditions.checkArgument(delayStrategy >= 0 && delayStrategy <= 4, "illegal delayStrategy:" + delayStrategy);
            this.delayStrategy = delayStrategy;
            return this;
        }

        public Setter withDelay(long delay) {
            Preconditions.checkArgument(delay > 0, "illegal delay:" + delay);
            this.delay = delay;
            return this;
        }

        public Setter withMinDelay(long minDelay) {
            Preconditions.checkArgument(minDelay > 0, "illegal minDelay:" + minDelay);
            this.minDelay = minDelay;
            return this;
        }

        public Setter withMaxDelay(long maxDelay) {
            Preconditions.checkArgument(maxDelay > 0, "illegal maxDelay:" + maxDelay);
            this.maxDelay = maxDelay;
            return this;
        }

        public Setter withMaxDuration(long maxDuration) {
            Preconditions.checkArgument(maxDuration > 0, "illegal maxDuration:" + maxDuration);
            this.maxDuration = maxDuration;
            return this;
        }

        public Setter withMultipier(double multiplier) {
            Preconditions.checkArgument(multiplier > 0, "illegal multiplier:" + multiplier);
            this.multiplier = multiplier;
            return this;
        }

        public Setter withIgnoreExceptions(Set<Class<? extends Throwable>> ignoreExceptions) {
            this.ignoredExceptions = ignoreExceptions;
            return this;
        }

        public Setter withRetryOnExceptions(Set<Class<? extends Throwable>> retryForExceptions) {
            this.retryForExceptions = retryForExceptions;
            return this;
        }

        public boolean isActive() {
            return active;
        }

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public int getDelayStrategy() {
            return delayStrategy;
        }

        public long getDelay() {
            return delay;
        }

        public long getMinDelay() {
            return minDelay;
        }

        public long getMaxDelay() {
            return maxDelay;
        }

        public long getMaxDuration() {
            return maxDuration;
        }

        public double getMultiplier() {
            return multiplier;
        }

        public Set<Class<? extends Throwable>> getRetryForExceptions() {
            return retryForExceptions;
        }

        public Set<Class<? extends Throwable>> getIgnoredExceptions() {
            return ignoredExceptions;
        }
    }
}
