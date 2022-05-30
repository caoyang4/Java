package src.rhino.system.reader;

import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;

/**
 * @description: DefaultSystemMetricReader
 * @author: zhangxiudong
 * @date: 2021-05-31
 **/
public class DefaultSystemMetricReader {

    private final OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

    public DefaultSystemMetricReader() {
    }

    public double getCpuUsage() {
        return this.operatingSystemMXBean.getSystemCpuLoad();
    }



}
