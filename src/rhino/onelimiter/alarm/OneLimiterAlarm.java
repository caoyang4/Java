package src.rhino.onelimiter.alarm;

import src.lion.client.ConfigEvent;
import src.lion.client.ConfigListener;
import src.lion.client.Lion;
import src.rhino.config.ConfigChangedListener;
import src.rhino.config.ConfigFactory;
import src.rhino.config.Configuration;
import src.rhino.dispatcher.RhinoEvent;
import src.rhino.dispatcher.RhinoEventDispatcher;
import src.rhino.limit.LimiterEventType;
import src.rhino.onelimiter.OneLimiterStrategy;
import src.rhino.util.AppUtils;

/**
 * Created by zhanjun on 2018/4/20.
 */
public class OneLimiterAlarm {

    private static Configuration config = ConfigFactory.getInstance();
    private static String appKey = AppUtils.getAppName();

    static {
        OneLimiterQpsCleaner.init();
    }

    private OneLimiterQpsMetric oneLimiterQpsMetric;
    private RhinoEventDispatcher eventDispatcher;
    private String eventTag;
    /**
     * warn 阈值
     */
    private static final String ALARM_RATE_KEY = appKey + ".rhino.oneLimiter.alarm.rate";
    private static volatile float alarmRate = config.getFloatValue(ALARM_RATE_KEY, 0.8f);
    /**
     * 上报开关
     */
    private static final String ALARM_SWITCH_KEY = appKey + ".rhino.oneLimiter.alarm.switch";
    private static volatile boolean alarmSwitchOpen = config.getBooleanValue(ALARM_SWITCH_KEY, true);
    /**
     * 上报数据间隔（秒）
     */
    private static final String ALARM_INTERVAL_KEY = "rhino.oneLimiter.alarm.interval.seconds";
    private static volatile int alarmIntervalSeconds = Lion.getIntValue(ALARM_INTERVAL_KEY, 60);

    static {
        Lion.addConfigListener(ALARM_INTERVAL_KEY, new ConfigListener() {
            @Override
            public void configChanged(ConfigEvent configEvent) {
                try {
                    alarmIntervalSeconds = Integer.parseInt(configEvent.getValue());
                } catch (Exception e) {
                    // ignore exception
                }
            }
        });

        config.addListener(ALARM_SWITCH_KEY, new ConfigChangedListener() {
            @Override
            public void invoke(String key, String oldValue, String newValue) {
                alarmSwitchOpen = Boolean.parseBoolean(newValue);
            }
        });

        config.addListener(ALARM_RATE_KEY, new ConfigChangedListener() {
            @Override
            public void invoke(String key, String oldValue, String newValue) {
                try {
                    alarmRate = Float.parseFloat(newValue);
                } catch (Exception e) {
                    // ignore exception
                }
            }
        });
    }

    public OneLimiterAlarm(OneLimiterStrategy strategy, RhinoEventDispatcher eventDispatcher) {
        this.oneLimiterQpsMetric = new OneLimiterQpsMetric(strategy);
        this.eventDispatcher = eventDispatcher;
        this.eventTag = strategy.getCatEventTag();
    }

    /**
     * 通过统计
     *
     * @param isTest
     */
    public void success(int tokenNum) {
        oneLimiterQpsMetric.markSuccess(tokenNum);
        eventDispatcher.dispatchEvent(new RhinoEvent(eventTag, LimiterEventType.ACCESS, tokenNum));
    }

    /**
     * 统计并判断是否预警
     *
     * @param threshold
     * @param tokenNum
     * @return
     */
    public boolean successAndCheckWarn(long threshold, int tokenNum) {
        long current = oneLimiterQpsMetric.markSuccess(tokenNum);
        eventDispatcher.dispatchEvent(new RhinoEvent(eventTag, LimiterEventType.ACCESS, tokenNum));

        if (checkWarning(current, threshold)) {
            warn(tokenNum);
            return true;
        }
        return false;
    }

    /**
     * 预警统计
     */
    public void warn(int tokenNum) {
        oneLimiterQpsMetric.markWarn(tokenNum);
        eventDispatcher.dispatchEvent(new RhinoEvent(eventTag, LimiterEventType.WARN, tokenNum));
    }

    /**
     * 拒绝统计
     */
    public void reject(int tokenNum) {
        oneLimiterQpsMetric.markReject(tokenNum);
        eventDispatcher.dispatchEvent(new RhinoEvent(eventTag, LimiterEventType.REFUSE, tokenNum));
    }

    /**
     * 判断是否需要阈值告警
     *
     * @param currentCount
     * @param threshold
     * @return
     */
    public static boolean checkWarning(long currentCount, long threshold) {
        // 如果阈值只有0~1，就没有必要预警了
        if (threshold <= 1) {
            return false;
        }
        float rate = alarmRate;
        return rate > 0 && currentCount >= rate * threshold;
    }

    public OneLimiterQpsMetric getOneLimiterQpsMetric() {
        return oneLimiterQpsMetric;
    }

    public static int getAlarmIntervalSeconds() {
        return alarmIntervalSeconds;
    }

    public static boolean isAlarmSwitchOpen() {
        return alarmSwitchOpen;
    }
}
