package src.rhino.system;

import src.rhino.config.ConfigChangedListener;
import src.rhino.config.ConfigFactory;
import src.rhino.log.Logger;
import src.rhino.log.LoggerFactory;
import src.rhino.timewindow.TimeWindowFactory;
import src.rhino.timewindow.system.SystemTimeWindow;
import src.rhino.timewindow.system.SystemTimeWindowData;
import src.rhino.util.AppUtils;
import src.rhino.util.CommonUtils;

import java.util.Arrays;
import java.util.List;

/**
 * @description: SystemMetrics
 * @author: zhangxiudong
 * @date: 2021-05-31
 **/
public class SystemMetrics {

    private static Logger logger = LoggerFactory.getLogger(SystemMetrics.class);

    private static SystemTimeWindow systemTimeWindow = TimeWindowFactory.newSystemTimeWindow("rhino-one-limiter");

    private static volatile double currentCpuUsage = -1.0D;

    private static volatile double topPercent = 0.5;

    private static volatile int windowLength = 100;

    private SystemMetrics() {
    }

    static {
        String topPercentKey = AppUtils.getAppName() + ".rhino-one-limiter.systemmetric.toppercent";
        String windowLengthKey = AppUtils.getAppName() + ".rhino-one-limiter.systemmetric.windowLength";
        topPercent = ConfigFactory.getInstance().getFloatValue(topPercentKey, 0.5f);
        windowLength = ConfigFactory.getInstance().getIntValue(windowLengthKey, 100);
        //addListener
        ConfigFactory.getInstance().addListener(topPercentKey, new ConfigChangedListener() {
            @Override
            public void invoke(String key, String oldValue, String newValue) {
                logger.info(String.format("topPercentKey changed oldValue=%s newValue=%s", oldValue, newValue));
                double value = Double.parseDouble(newValue);
                if (value > 0 && value < 1) {
                    topPercent = value;
                }
            }
        });
        ConfigFactory.getInstance().addListener(windowLengthKey, new ConfigChangedListener() {
            @Override
            public void invoke(String key, String oldValue, String newValue) {
                logger.info(String.format("windowLengthKey changed oldValue=%s newValue=%s", oldValue, newValue));
                int value = Integer.parseInt(newValue);
                if (windowLength > 0 && value < 160) {
                    windowLength = value;
                }
            }
        });
    }


    public static void setCurrentCpuUsage(double cpuUsage, long timeMillis) {
        if (cpuUsage >= 0.0D) {
            currentCpuUsage = cpuUsage;
            systemTimeWindow.setCurrentCpuUsage(cpuUsage, timeMillis);
        } else {
            //cpu利用率采集失败，设置无效值，避免误限问题
            //比如 前一次采集cpu利用率90%达到阈值，后面采集失败，导致一直触发限流
            currentCpuUsage = -1.0D;
        }
    }

    public static double getCurrentCpuUsage() {
        return currentCpuUsage;
    }

    public static double getAverageCpuUsage() {
        List<SystemTimeWindowData> systemTimeWindowDatas =
                systemTimeWindow.values(CommonUtils.currentMillis(), windowLength);
        double sum = 0;
        double size = systemTimeWindowDatas.size();
        if (size < 1) {
            return -1.0D;
        }
        for (SystemTimeWindowData systemTimeWindowData : systemTimeWindowDatas) {
            sum += systemTimeWindowData.getCpuUsage();
        }
        return sum / size;
    }

    public static double getMaxCpuUsage() {
        List<SystemTimeWindowData> systemTimeWindowDatas =
                systemTimeWindow.values(CommonUtils.currentMillis(), windowLength);
        double max = -1.0D;
        for (SystemTimeWindowData systemTimeWindowData : systemTimeWindowDatas) {
            max = Math.max(max, systemTimeWindowData.getCpuUsage());
        }
        return max;
    }

    public static double getMinCpuUsage() {
        List<SystemTimeWindowData> systemTimeWindowDatas =
                systemTimeWindow.values(CommonUtils.currentMillis(), windowLength);
        if (systemTimeWindowDatas.size() < 1) {
            return -1.0D;
        }
        double min = 1D;
        for (SystemTimeWindowData systemTimeWindowData : systemTimeWindowDatas) {
            min = Math.min(min, systemTimeWindowData.getCpuUsage());
        }
        return min;
    }

    public static double getTopPercent() {
        List<SystemTimeWindowData> systemTimeWindowDatas =
                systemTimeWindow.values(CommonUtils.currentMillis(), windowLength);
        double[] cpuUsages = new double[systemTimeWindowDatas.size()];
        int index = 0;
        for (SystemTimeWindowData systemTimeWindowData : systemTimeWindowDatas) {
            cpuUsages[index] = systemTimeWindowData.getCpuUsage();
            index++;
        }
        Arrays.sort(cpuUsages);
        return cpuUsages[Math.max(0, Math.min((int) (cpuUsages.length * topPercent), index))];
    }

}
