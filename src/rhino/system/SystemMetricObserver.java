package src.rhino.system;

/**
 * @description: SystemMetricExtension
 * @author: zhangxiudong
 * @date: 2021-05-31
 **/
public interface SystemMetricObserver {

    void onCpuUsageCollected(double cpuUsage);
}
