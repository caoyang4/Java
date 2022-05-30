package src.rhino.timewindow.system;

import src.rhino.timewindow.TimeWindow;

/**
 * @description: SystemTimeWindowBucket
 * @author: zhangxiudong
 * @date: 2021-11-08
 **/
public class SystemTimeWindow extends TimeWindow<SystemTimeWindowData> {


    public SystemTimeWindow(String rhinoKey, long timeLengthInMillis, int bucketCount) {
        super(rhinoKey, timeLengthInMillis, bucketCount);
    }

    @Override
    protected SystemTimeWindowData newData() {
        return new SystemTimeWindowData();
    }

    public void setCurrentCpuUsage(double cpuUsage,long timeMillis) {
        currentBucket(timeMillis).getData().setCpuUsage(cpuUsage);
    }

    public double getCurrentCpuUsage() {
        return currentBucket().getData().getCpuUsage();
    }
}
