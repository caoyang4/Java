package src.rhino.service;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


import src.rhino.Rhino;
import src.rhino.log.Logger;
import src.rhino.log.LoggerFactory;

/**
 * 定时上报
 */
public class ReportTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ReportTask.class);
    private static ReportService reportService = new ReportService();
    // LION动态配置，默认60分钟上报一次
    private static String reportIntervalKey = "rhino.report.interval.v2";
    private static int reportInterval = 60;
    private RhinoEntity rhinoEntity;
    private ScheduledThreadPoolExecutor executor;

    public ReportTask(RhinoEntity rhinoEntity, ScheduledThreadPoolExecutor executor) {
        this.rhinoEntity = rhinoEntity;
        this.executor = executor;
    }

    @Override
    public void run() {
        try {
            doReport(rhinoEntity);
        } catch (Exception e) {
            //ignore exception
        }

        executor.schedule(this, 1, TimeUnit.MINUTES);
    }

    /**
     * @param rhinoEntity
     * @throws Exception
     */
    private void doReport(RhinoEntity rhinoEntity) {
        StringBuilder builder = new StringBuilder();
        builder.append("appKey=" + rhinoEntity.getAppKey());
        builder.append("&rhinoKey=" + rhinoEntity.getRhinoKey());
        builder.append("&useMode=" + rhinoEntity.getUseMode());
        builder.append("&set=" + rhinoEntity.getSet());
        builder.append("&type=" + rhinoEntity.getType().getValue());
        builder.append("&configName=" + rhinoEntity.getConfigName());
        builder.append("&version=" + Rhino.VERSION);

        String event = builder.toString();
        String properties = rhinoEntity.getProperties();
        if (properties != null) {
            builder.append("&properties=" + properties);
        }
        int i = 1;
        for (; i <= 3; i++) {
            try {
                reportService.doPost(builder.toString());
                break;
            } catch (Exception e) {
                //ignore exception
            }
            try {
                TimeUnit.SECONDS.sleep(i);
            } catch (InterruptedException e) {
                //ignore exception
            }
        }
//        Cat.logEvent("Rhino.Report", event + "&try=" + i);
    }

    private static class ReportService extends RhinoService {

        private static String method = "report";

        public ReportService() {
            super(method);
        }
    }
}