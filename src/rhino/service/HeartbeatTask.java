package src.rhino.service;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import src.rhino.log.Logger;
import src.rhino.log.LoggerFactory;
import src.rhino.util.AppUtils;

/**
 * @author zhanjun
 */
public class HeartbeatTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(HeartbeatTask.class);
    private static OnlineService onlineService = new OnlineService();
    private static String heartbeatIntervalKey = "rhino.heartbeat.interval";
    private static int heartbeatInterval = 1000 * 10;
    private ScheduledThreadPoolExecutor executor;
    private int port;

    public HeartbeatTask(ScheduledThreadPoolExecutor executor, int port) {
        this.executor = executor;
        this.port = port;
    }

    @Override
    public void run() {
        doHeartbeat();
        int delay = heartbeatInterval;
        if (delay > 0) {
            executor.schedule(this, delay, TimeUnit.MILLISECONDS);
        }
    }

    private void doHeartbeat() {
        StringBuilder builder = new StringBuilder();
        builder.append("&appKey=").append(AppUtils.getAppName());
        builder.append("&ip=").append(AppUtils.getLocalIp());
        builder.append("&hostName=").append(AppUtils.getHostName());
        builder.append("&setName=").append(AppUtils.getSet());
        builder.append("&port=").append(port);
        String params = builder.toString();
        int i = 1;
        for (; i <= 3; i++) {
            try {
                onlineService.doPost(params);
                break;
            } catch (Exception e) {
                //Do nothing
            }
            try {
                TimeUnit.SECONDS.sleep(i);
            } catch (InterruptedException e) {
                //Do nothing
            }
        }
    }

    public static class OnlineService extends RhinoService {

        private static String method = "online";

        public OnlineService() {
            super(method);
        }
    }
}