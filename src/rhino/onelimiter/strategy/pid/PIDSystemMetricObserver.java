package src.rhino.onelimiter.strategy.pid;

import src.rhino.log.Logger;
import src.rhino.log.LoggerFactory;
import src.rhino.onelimiter.strategy.AdaptiveVmPIDStrategy;
import src.rhino.system.SystemMetricObserver;

import java.util.HashMap;
import java.util.Map;

/**
 * @description: PIDSystemMetricObserver
 * @author: zhangxiudong
 * @date: 2021-05-31
 **/
public class PIDSystemMetricObserver implements SystemMetricObserver {

    private static Logger logger = LoggerFactory.getLogger(PIDSystemMetricObserver.class);

    private static final long DEFAULT_SAMPLE_LOG_INTERVAL_MS = 200L;

    private static final double DEFAULT_P = 0.5D;

    private static final double DEFAULT_I = 0.3D;

    private static final double DEFAULT_D = 0.2D;

    private static volatile double cpuTarget = 0.6D;

    private volatile long lastSampleTimestamp = 0L;

    private PIDController pidController = new PIDController(DEFAULT_P, DEFAULT_I, DEFAULT_D);

    private static Map<String, String> tags = new HashMap<>();

    static {
        tags.put("ip", "localhost");
    }

    public PIDSystemMetricObserver() {
    }


    @Override
    public void onCpuUsageCollected(double cpuUsage) {
        double output = this.pidController.calcOutput(cpuUsage, cpuTarget);
        double p = this.convertCpuActionToProbability(cpuUsage, output);
        long curTime = System.currentTimeMillis();
        if (curTime - this.lastSampleTimestamp >= DEFAULT_SAMPLE_LOG_INTERVAL_MS) {
            if (p < 1.0D) {
                logger.info(String.format("[Sample CpuUsageCollected] cpuusage=%.3f, output=%.3f, percent=%.2f", cpuUsage, output, p));
                this.lastSampleTimestamp = curTime;
            }
        }
        AdaptiveVmPIDStrategy.setProbability(p);

    }

    private double convertCpuActionToProbability(double curCpu, double output) {
        if (output >= 0.0D) {
            return 1.0D;
        }
        double e = 1.0E-4D;
        if (curCpu >= -e && curCpu <= e) {
            return 1.0D;
        }
        double negativeFeedback = Math.max(-curCpu, Math.min(output, 0.0D));
        return (curCpu + negativeFeedback) / curCpu;
    }

    public static double getCpuTarget() {
        return PIDSystemMetricObserver.cpuTarget;
    }

    public static void setCpuTarget(double cpuTarget) {
        PIDSystemMetricObserver.cpuTarget = cpuTarget;
    }


}
