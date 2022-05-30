package src.rhino.threadpool.job;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import src.rhino.log.Logger;
import src.rhino.log.LoggerFactory;
import src.rhino.threadpool.RhinoThreadPoolMetric;
import src.rhino.threadpool.alarm.ThreadPoolAlarmData;
import src.rhino.threadpool.alarm.ThreadPoolAlarmManager;
import src.rhino.timewindow.threadpool.ThreadPoolTimeWindow;
import src.rhino.util.AppUtils;
//import src.rhino.util.SerializerUtils;

/**
 * 定时扫描并推送线程池告警
 * Created by zmz on 2020/11/16.
 */
public class RhinoThreadPoolAlarmScanner implements RhinoScheduledTask {

    private static final Logger logger = LoggerFactory.getLogger(RhinoThreadPoolAlarmScanner.class);
    private static RhinoThreadPoolAlarmReportService reportService = new RhinoThreadPoolAlarmReportService();

    private ThreadPoolAlarmManager alarmManager = ThreadPoolAlarmManager.getInstance();
    private final int period = 30;

    @Override
    public String name() {
        return "RhinoThreadPoolAlarmScanner";
    }

    @Override
    public long initialDelay() {
        return this.period;
    }

    @Override
    public long period() {
        return this.period;
    }

    @Override
    public TimeUnit periodUnit() {
        return TimeUnit.SECONDS;
    }

    @Override
    public void run() {
        List<ThreadPoolAlarmData> alarmList = new ArrayList<>();
        for (ThreadPoolTimeWindow metric : RhinoThreadPoolMetric.minuteMetric.values()) {
            try{
                ThreadPoolAlarmData alarmMsg = alarmManager.checkIfAlarm(metric.getRhinoKey(), metric, period);
                if (alarmMsg != null) {
                    alarmList.add(alarmMsg);
                }
            }catch (Exception e) {
                logger.error("failed to check threadpool alarm: " + metric.getRhinoKey(), e);
            }
        }
        pageablePushAlarmMsg(alarmList, 10);
    }

    /**
     * 分批推送告警消息，避免一次性请求过大
     *
     * @param alarmList
     * @param pageSize
     */
    private void pageablePushAlarmMsg(List<ThreadPoolAlarmData> alarmList, int pageSize) {
        if (alarmList == null || alarmList.isEmpty()) {
            return;
        }

        List<ThreadPoolAlarmData> readyToPush = new ArrayList<>(pageSize);
        Iterator<ThreadPoolAlarmData> iterator = alarmList.iterator();
        while(iterator.hasNext()){
            readyToPush.add(iterator.next());

            if(readyToPush.size() == pageSize || !iterator.hasNext()){
                pushAlarmMsg(readyToPush);
                readyToPush.clear();
            }
        }
    }

    private void pushAlarmMsg(List<ThreadPoolAlarmData> alarmList) {
        if(alarmList == null || alarmList.isEmpty()){
            return;
        }

        try {
//            String alarmDetail = SerializerUtils.write(alarmList);
            String alarmDetail = "alarm";
            StringBuilder builder = new StringBuilder();
            builder.append("&ip=" + AppUtils.getLocalIp());
            builder.append("&appKey=" + AppUtils.getAppName());
            builder.append("&set=" + AppUtils.getSet());
            builder.append("&alarms=" + alarmDetail);
            reportService.doPost(builder.toString());
        } catch (Exception e) {
            logger.error("pushAlarmMsg exception: ", e);
        }
    }
}
