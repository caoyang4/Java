package src.rhino.threadpool.alarm;

import src.rhino.timewindow.threadpool.ThreadPoolProfilingSummary;

/**
 * Created by zmz on 2020/11/12.
 */
public class ThreadPoolAlarmRule {

    private boolean active;

    /**
     * 告警等级，P0，P1，P2，P3
     */
    private int alarmLevel;

    /**
     * 告警数据时间窗口，大小范围限定1~60秒，兼容旧版本，默认是1秒
     * @Since 1.2.8
     */
    private int timeLength = 1;

    /**
     * 最大已创建线程数量告警阈值
     */
    private int poolSizeThreshold;

    /**
     * 最大阻塞队列中排队任务数量告警阈值
     */
    private int queueSizeThreshold;

    /**
     * 总拒绝任务数量告警阈值
     */
    private int rejectCountThreshold;

    /**
     * 最大活跃线程数量告警阈值，兼容就版本，默认999不会触发
     * @since 1.2.8
     */
    private int activeCountThreshold = 999;

    public int getPoolSizeThreshold() {
        return poolSizeThreshold;
    }

    public void setPoolSizeThreshold(int poolSizeThreshold) {
        this.poolSizeThreshold = poolSizeThreshold;
    }

    public int getQueueSizeThreshold() {
        return queueSizeThreshold;
    }

    public void setQueueSizeThreshold(int queueSizeThreshold) {
        this.queueSizeThreshold = queueSizeThreshold;
    }

    public int getRejectCountThreshold() {
        return rejectCountThreshold;
    }

    public void setRejectCountThreshold(int rejectCountThreshold) {
        this.rejectCountThreshold = rejectCountThreshold;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getAlarmLevel() {
        return alarmLevel;
    }

    public void setAlarmLevel(int alarmLevel) {
        this.alarmLevel = alarmLevel;
    }

    public int getTimeLength() {
        return timeLength;
    }

    public void setTimeLength(int timeLength) {
        this.timeLength = timeLength;
    }

    public int getActiveCountThreshold() {
        return activeCountThreshold;
    }

    public void setActiveCountThreshold(int activeCountThreshold) {
        this.activeCountThreshold = activeCountThreshold;
    }

    /**
     * 检查统计数据是否触发了告警
     * @param summary
     * @return
     */
    public int check(ThreadPoolProfilingSummary summary){
        int type = 0;
        if (summary.getMaxPoolSize() >= poolSizeThreshold) {
            type += ThreadPoolAlarmType.TOO_MANNY_THREAD.getValue();
        }
        if (summary.getMaxActiveCount() >= activeCountThreshold) {
            type += ThreadPoolAlarmType.TOO_MANNY_ACTIVE_THREAD.getValue();
        }
        if (summary.getMaxQueueSize() >= queueSizeThreshold) {
            type += ThreadPoolAlarmType.QUEUE_TOO_BIG.getValue();
        }
        if (summary.getRejectCount() >= rejectCountThreshold) {
            type += ThreadPoolAlarmType.TOO_MANNY_REJECT.getValue();
        }
        return type;
    }
}
