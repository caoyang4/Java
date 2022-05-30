package src.rhino.system.reader;

import src.lion.client.util.NamedThreadFactory;
import src.rhino.log.Logger;
import src.rhino.log.LoggerFactory;
import com.mysql.cj.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @description: HULK容器系统指标计算
 **/
public class HulkSystemMetricReader {

    private static Logger logger = LoggerFactory.getLogger(HulkSystemMetricReader.class);

    private final ExecutorService executorService = Executors.
            newSingleThreadExecutor(new NamedThreadFactory("rhino-hulk-system-metric-reader-task", true));
    /**
     * 容器cpu相关指标
     */
    private static final String CGROUP_CPUACCT_USAGE_DIR = "/sys/fs/cgroup/cpu/cpuacct.usage";
    private static final String CGROUP_CPU_SHARE_DIR = "/sys/fs/cgroup/cpu/cpu.shares";
    private static final String CGROUP_CPU_QUOTA_DIR = "/sys/fs/cgroup/cpu/cpu.cfs_quota_us";
    private static final String CGROUP_CPU_PERIOD_DIR = "/sys/fs/cgroup/cpu/cpu.cfs_period_us";
    /**
     * cpu核心数
     */
    private double cpuCores = -1.0D;
    /**
     * 当前容器cpu使用率
     */
    private volatile double cpuUsage = -1.0D;
    /**
     * 前一次采集cpuacct.usage的值
     */
    private long prevCpuTotal = -1L;
    /**
     * 采样周期，单位是毫秒, 默认50ms采样一次，1s采集20次
     */
    private int periodMs = 50;
    /**
     * 每毫秒内所有cpu执行纳秒的时间和
     * 1ms=1000*1000ns
     */
    private double tickPerMillis = -1.0D;

    public HulkSystemMetricReader() {
        this.init();
    }

    public HulkSystemMetricReader(int periodMs) {
        this.periodMs = periodMs;
        this.init();
    }

    public void init() {
        long cpuPeriod = this.readLongFromFile(CGROUP_CPU_PERIOD_DIR);
        long cpuQuota = this.readLongFromFile(CGROUP_CPU_QUOTA_DIR);
        long cpuShares = this.readLongFromFile(CGROUP_CPU_SHARE_DIR);
        if (cpuQuota > 0L && cpuPeriod > 0L) {
            this.cpuCores = (double) cpuQuota * 1.0D / (double) cpuPeriod;
        } else if (cpuShares > 0L) {
            this.cpuCores = (double) cpuShares * 1.0D / 1024.0D;
        }
        if (this.cpuCores > 0.0D) {
            //cpu核心数*1ms的ns值
            this.tickPerMillis = this.cpuCores * 1000.0D * 1000.0D;
            this.executorService.submit(new Runnable() {
                @Override
                public void run() {
                    HulkSystemMetricReader.this.loopCollect();
                }
            }) ;
        }
    }

    private void loopCollect() {
        while (true) {
            try {
                long curCgroupCpuAcctUsage = this.readLongFromFile(CGROUP_CPUACCT_USAGE_DIR);
                if (curCgroupCpuAcctUsage >= 0L) {
                    if (this.prevCpuTotal > 0L) {
                        double tick = this.tickPerMillis * (double) this.periodMs;
                        double percentage = Math.max(0.0D, (double) (curCgroupCpuAcctUsage - this.prevCpuTotal) / tick);
                        this.cpuUsage = Math.min(percentage, 1.0D);
                    }
                    this.prevCpuTotal = curCgroupCpuAcctUsage;
                }
            } catch (Throwable t) {
                logger.warn("[HulkSystemMetricCollectorTask] Failed to get and update container system metrics", t);
            }
            try {
                Thread.sleep(this.periodMs);
            } catch (InterruptedException e) {
                //do nothing
            }
        }
    }


    private long readLongFromFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return -1L;
        }
        try {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String value = reader.readLine();
                return StringUtils.isNullOrEmpty(value) ? -1L : Long.parseLong(value);
            }
        } catch (Exception e) {
            logger.warn("[HulkSystemMetricReader] Failed to read cgroup file: " + path, e);
            return -1L;
        }
    }


    public double getCpuUsage() {
        return this.cpuUsage;
    }


}
