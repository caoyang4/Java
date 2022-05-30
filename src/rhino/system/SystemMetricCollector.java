package src.rhino.system;

import src.rhino.config.ConfigFactory;
import src.rhino.log.Logger;
import src.rhino.log.LoggerFactory;
import src.rhino.system.reader.HulkSystemMetricReader;
import src.rhino.system.reader.DefaultSystemMetricReader;
import src.rhino.util.AppUtils;
import src.rhino.util.ContainerUtil;
import src.rhino.util.ExtensionLoader;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @description: SystemMetricCollector
 * @author: zhangxiudong
 * @date: 2021-05-31
 **/
public class SystemMetricCollector {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public SystemMetricCollector() {
        this.executorService.submit(new SystemMetricCollectorTask());
    }

    public static class SystemMetricCollectorTask implements Runnable {

        private static Logger logger = LoggerFactory.getLogger(SystemMetricCollectorTask.class);
        /**
         * cpu使用率指标采集间隔时间
         */
        private int cpuUsageUpdateIntervalMs = ConfigFactory.getInstance().getIntValue(AppUtils.getAppName() + ".rhino.cpuusage.interval", 50);

        private final List<SystemMetricObserver> metricExtensions = ExtensionLoader.getExtensionList(SystemMetricObserver.class);

        private final boolean isDocker = ContainerUtil.isDocker();

        private final DefaultSystemMetricReader defaultMetricReader = new DefaultSystemMetricReader();

        private HulkSystemMetricReader containerMetricReader;

        public SystemMetricCollectorTask() {
            if (this.isDocker) {
                this.containerMetricReader = new HulkSystemMetricReader(cpuUsageUpdateIntervalMs);
                logger.info("[SystemMetricCollectorTask] Container env detected, using container system metric collector");
            }
        }

        @Override
        public void run() {
            while (true) {
                try {
                    double currentCpuUsage = this.isDocker ? this.containerMetricReader.getCpuUsage() : this.defaultMetricReader.getCpuUsage();
                    SystemMetrics.setCurrentCpuUsage(currentCpuUsage, System.currentTimeMillis());
                    if (currentCpuUsage >= 0.0D) {
                        this.onCpuUsageCollected(currentCpuUsage);
                    }
                } catch (Throwable t) {
                    logger.warn("[SystemMetricCollectorTask] Failed to get system metrics", t);
                }
                try {
                    Thread.sleep(this.cpuUsageUpdateIntervalMs);
                } catch (InterruptedException e) {
                    //ignore exception
                }
            }
        }

        private void onCpuUsageCollected(double cpuUsage) {
            if (this.metricExtensions.isEmpty()) {
                return;
            }
            for (SystemMetricObserver extension : this.metricExtensions) {
                extension.onCpuUsageCollected(cpuUsage);
            }
        }
    }
}
