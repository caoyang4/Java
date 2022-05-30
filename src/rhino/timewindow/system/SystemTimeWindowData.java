package src.rhino.timewindow.system;

import src.rhino.timewindow.TimeWindowData;

/**
 * @description: CpuUsageTimeWindowData
 * @author: zhangxiudong
 * @date: 2021-11-08
 **/
public class SystemTimeWindowData implements TimeWindowData {

    private volatile double cpuUsage;


    public SystemTimeWindowData() {
    }

    public SystemTimeWindowData(double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public double getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    @Override
    public void reset() {
        cpuUsage = 0;

    }
}
