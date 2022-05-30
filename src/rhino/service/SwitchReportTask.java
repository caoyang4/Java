package src.rhino.service;

import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

import src.rhino.Rhino;
import src.rhino.Switch.SwitchEntity;
import src.rhino.log.Logger;
import src.rhino.log.LoggerFactory;
import src.rhino.util.AppUtils;

/**
 * Created by zhanjun on 2018/4/17.
 */
public class SwitchReportTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(SwitchReportTask.class);
    private static SwitchService switchService = new SwitchService();
    private SwitchEntity switchEntity;

    public SwitchReportTask(SwitchEntity switchEntity) {
        this.switchEntity = switchEntity;
    }

    @Override
    public void run() {
        StringBuilder builder = new StringBuilder();
        builder.append("&appKey=").append(switchEntity.getAppKey());
        builder.append("&key=").append(switchEntity.getKey());
        try {
            builder.append("&value=").append(URLEncoder.encode(switchEntity.getValue(), "UTF-8"));
        } catch (Exception e) {
            logger.warn("switch value encode failed", e);
        }
        builder.append("&configName=").append(switchEntity.getConfigName());
        builder.append("&setName=").append(AppUtils.getSet());
        builder.append("&version=").append( Rhino.VERSION);


        int i = 1;
        for (; i <= 3; i++) {
            try {
                switchService.doPost(builder.toString());
                break;
            } catch (Exception e) {
                logger.warn("report switch failed : " + builder.toString(), e);
            }
            try {
                TimeUnit.SECONDS.sleep(i);
            } catch (InterruptedException e) {
                //ignore exception
            }
        }
        String event = builder.toString();
    }

    private static class SwitchService extends RhinoService {

        private static String method = "switchReport";

        public SwitchService() {
            super(method);
        }
    }
}
