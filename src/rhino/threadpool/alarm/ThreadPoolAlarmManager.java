package src.rhino.threadpool.alarm;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import src.rhino.timewindow.threadpool.ThreadPoolProfilingSummary;
import src.rhino.timewindow.threadpool.ThreadPoolTimeWindow;
import src.rhino.util.CommonUtils;
import com.mysql.cj.util.StringUtils;

import src.rhino.RhinoConfigProperties;
import src.rhino.config.ConfigChangedListener;
import src.rhino.log.Logger;
import src.rhino.log.LoggerFactory;
import src.rhino.timewindow.threadpool.ThreadPoolProfilingData;
import src.rhino.timewindow.TimeWindowBucket;
import src.rhino.util.AppUtils;

/**
 * 线程池告警监控
 * 实时告警会暂时缓存在队列中，然后由Job定时推送
 * Created by zmz on 2020/11/12.
 */
public class ThreadPoolAlarmManager extends RhinoConfigProperties {

    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolAlarmManager.class);
    private static ThreadPoolAlarmManager instance = new ThreadPoolAlarmManager();

    //告警规则动态管理
    private String alarmRulesKey;
    private Map<String, ThreadPoolAlarmRule> rules = new ConcurrentHashMap<>();

    private ThreadPoolAlarmManager() {
        super(AppUtils.getAppName(), null, null, null);
        this.alarmRulesKey = appKey + ".rhino.threadpool.alarm";
        getAndListen();
    }

    public static ThreadPoolAlarmManager getInstance() {
        return instance;
    }

    /**
     * 初始化并监听告警配置
     */
    private void getAndListen() {
        //加载并监听告警配置
        updateAlarmRules(getStringValue(alarmRulesKey, ""));
        listenAlarmRules();
    }

    private void updateAlarmRules(String value) {
        if (StringUtils.isNullOrEmpty(value)) {
            rules.clear();
        }
    }

    private void listenAlarmRules() {
        addPropertiesChangedListener(alarmRulesKey, new ConfigChangedListener() {
            @Override
            public void invoke(String key, String oldValue, String newValue) {
                logger.info("RhinoThreadPool alarm rules changed: " + oldValue + "[before], " + newValue + "[after]");
                updateAlarmRules(newValue);
            }
        });
    }

    /**
     * 检查线程池统计数据是否需要告警
     * @param rhinoKey
     * @param summary
     * @param buckets
     * @return
     */
    public ThreadPoolAlarmData checkIfAlarm(String rhinoKey, ThreadPoolTimeWindow metric, int period) {
        ThreadPoolAlarmRule rule = getRule(rhinoKey);
        if (rule == null || !rule.isActive()) {
            return null;
        }

        ThreadPoolProfilingSummary summary = metric.scan(period, rule.getTimeLength());
        int checkResult = rule.check(summary);
        if(checkResult <= 0){
            return null;
        }

        long time = CommonUtils.currentMillis();
        List<TimeWindowBucket<ThreadPoolProfilingData>> detail = metric.buckets(time, 60);
        ThreadPoolAlarmData alarmMsg = new ThreadPoolAlarmData();
        alarmMsg.setAppKey(appKey);
        alarmMsg.setRhinoKey(rhinoKey);
        alarmMsg.setLevel(rule.getAlarmLevel());
        alarmMsg.setTriggers(checkResult);
        alarmMsg.setTimestamp(time);
        alarmMsg.setDetail(bucketsToString(detail));
        return alarmMsg;
    }


    /**
     * 根据rhionKey获取告警规则
     *
     * @param rhinoKey
     * @return 如果没有指定配置或者没有指定规则没有开启，则返回默认规则，如果默认规则也没有就返回null
     */
    private ThreadPoolAlarmRule getRule(String rhinoKey) {
        ThreadPoolAlarmRule trigger = rules.get(rhinoKey);
        return trigger == null || !trigger.isActive() ? globalRule() : trigger;
    }

    /**
     * AppKey维度，默认的告警规则
     *
     * @return
     */
    private ThreadPoolAlarmRule globalRule() {
        return rules.get("*");
    }

    /**
     * 压缩序列化线程池统计数据
     * @param buckets
     * @return
     */
    private String bucketsToString(List<TimeWindowBucket<ThreadPoolProfilingData>> buckets){
        if(buckets == null || buckets.isEmpty()){
            return "";
        }

        StringBuilder encodeBuckets = new StringBuilder(buckets.size() * 20);
        encodeBuckets.append("[");
        for(TimeWindowBucket<ThreadPoolProfilingData> bucket : buckets){
            encodeBuckets.append("{")
                    .append("\"time\":").append(bucket.getStartTime()).append(",")
                    .append("\"data\":[")
                    .append(bucket.getData().getTaskCount()).append(",")
                    .append(bucket.getData().getTotalWaitTime()).append(",")
                    .append(bucket.getData().getTotalExecuteTime()).append(",")
                    .append(bucket.getData().getMaxWaitTime()).append(",")
                    .append(bucket.getData().getMaxExecuteTime()).append(",")
                    .append(bucket.getData().getRejectCount()).append(",")
                    .append(bucket.getData().getMaxPoolSize()).append(",")
                    .append(bucket.getData().getMaxActiveCount()).append(",")
                    .append(bucket.getData().getMaxQueueSize())
                    .append("]")
                    .append("},");
        }
        encodeBuckets.deleteCharAt(encodeBuckets.length() - 1);
        encodeBuckets.append("]");
        return encodeBuckets.toString();
    }
}
