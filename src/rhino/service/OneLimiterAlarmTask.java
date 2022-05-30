package src.rhino.service;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

import org.springframework.util.CollectionUtils;

import src.rhino.log.Logger;
import src.rhino.log.LoggerFactory;
import src.rhino.onelimiter.alarm.OneLimiterAlarmEntity;
import src.rhino.util.AppUtils;
import src.rhino.util.SerializerUtils;

/**
 * Created by zhanjun on 2018/4/17.
 */
public class OneLimiterAlarmTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(OneLimiterAlarmTask.class);
    private static OneLimiterAlarmService service = new OneLimiterAlarmService();
    private OneLimiterAlarmEntity entity;

    public OneLimiterAlarmTask(OneLimiterAlarmEntity entity) {
        this.entity = entity;
    }

    @Override
    public void run() {
        StringBuilder builder = new StringBuilder();
        builder.append("&ip=" + AppUtils.getLocalIp());
        builder.append("&setName=" + AppUtils.getSet());
        builder.append("&appKey=" + entity.getAppKey());
        builder.append("&rhinoKey=" + entity.getRhinoKey());
        builder.append("&strategy=" + entity.getStrategy());
        try {
            builder.append("&entrance=" + URLEncoder.encode(entity.getEntrance(), "UTF-8"));
        } catch (Exception e) {
            //ignore exception
        }
        if (entity.getParams() != null) {
            try {
                builder.append("&params=" + URLEncoder.encode(entity.getParams(), "UTF-8"));
            } catch (Exception e) {
                //ignore exception
            }
        }
        builder.append("&type=" + entity.getType());
        builder.append("&count=" + entity.getCount());
        if (!CollectionUtils.isEmpty(entity.getQpsEntityList())) {
            try {
                builder.append("&qpsEntityList=" + SerializerUtils.write(entity.getQpsEntityList()));
            } catch (IOException e) {
                //ignore exception
            }
        }

        for (int i = 1; i <= 3; i++) {
            try {
                service.doPost(builder.toString());
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

    private static class OneLimiterAlarmService extends RhinoService {

        private static String method = "oneLimiterAlarm";

        public OneLimiterAlarmService() {
            super(method);
        }
    }
}
