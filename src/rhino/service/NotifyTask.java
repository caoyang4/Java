package src.rhino.service;

import java.util.concurrent.TimeUnit;

import src.rhino.dispatcher.NotifyEvent;
import src.rhino.log.Logger;
import src.rhino.log.LoggerFactory;
import src.rhino.util.AppUtils;

/**
 * Created by zhanjun on 2017/09/02.
 */
public class NotifyTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(NotifyTask.class);
    private static NotifyService notifyService = new NotifyService();
    private NotifyEvent notifyEvent;

    public NotifyTask(NotifyEvent notifyEvent) {
        this.notifyEvent = notifyEvent;
    }

    @Override
    public void run() {
        StringBuilder builder = new StringBuilder();
        builder.append("&ip=" + AppUtils.getLocalIp());
        builder.append("&appKey=" + AppUtils.getAppName());
        builder.append("&rhinoType=" + notifyEvent.getRhinoType().getValue());
        builder.append("&rhinoKey=" + notifyEvent.getRhinoKey());
        builder.append("&eventType=" + notifyEvent.getEventType().getIndex());
        builder.append("&eventData=" + notifyEvent.getData());
        builder.append("&eventTime=" + notifyEvent.getEventTime());

        for (int i = 1; i <= 3; i++) {
            try {
                notifyService.doPost(builder.toString());
                return;
            } catch (Exception e) {
                logger.warn("notify event failed : " + builder.toString(), e);
            }
            try {
                TimeUnit.SECONDS.sleep(i);
            } catch (InterruptedException e) {
                //ignore exception
            }
        }
    }

    private static class NotifyService extends RhinoService {

        private static String method = "notify";

        public NotifyService() {
            super(method);
        }
    }
}
