package src.rhino.service;


import java.util.concurrent.TimeUnit;

import src.rhino.log.Logger;
import src.rhino.log.LoggerFactory;
import src.rhino.onelimiter.OneLimiterEntity;
import src.rhino.util.AppUtils;

/**
 * Created by zhanjun on 2018/4/17.
 */
public class OneLimiterReportTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(OneLimiterReportTask.class);
    private static OneLimiterService flowLimiterService = new OneLimiterService();
    private OneLimiterEntity entity;

    public OneLimiterReportTask(OneLimiterEntity flowLimiterEntity) {
        this.entity = flowLimiterEntity;
    }

    @Override
    public void run() {
        StringBuilder result = new StringBuilder();
        result.append("&appKey=" + entity.getAppKey());
        result.append("&rhinoKey=" + entity.getRhinoKey());
        result.append("&path=" + entity.getPath());
        result.append("&setName=" + AppUtils.getSet());

        for (int i = 1; i <= 3; i++) {
            try {
                flowLimiterService.doPost(result.toString());
                return;
            } catch (Exception e) {
                //ignore exception
            }
            try {
                TimeUnit.SECONDS.sleep(i);
            } catch (InterruptedException e) {
                //ignore exception
            }
        }
    }

    private static class OneLimiterService extends RhinoService {

        private static String method = "oneLimiterReport";

        public OneLimiterService() {
            super(method);
        }
    }
}
