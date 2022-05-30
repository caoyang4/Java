package src.rhino.service;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import src.rhino.Switch.SwitchEntity;
import src.rhino.dispatcher.NotifyEvent;
import src.rhino.log.Logger;
import src.rhino.log.LoggerFactory;
import src.rhino.onelimiter.OneLimiterEntity;
import src.rhino.onelimiter.alarm.OneLimiterAlarmEntity;
import src.rhino.server.RhinoHttpServer;
import src.rhino.threadpool.ThreadPool;

/**
 * @author zhanjun on 2017/5/24.
 */
public final class RhinoManager {
    private static final Logger logger = LoggerFactory.getLogger(RhinoManager.class);
    private static ScheduledThreadPoolExecutor executor;

    private static Pattern keyPattern = Pattern.compile("[a-zA-Z0-9_.-]+");

    static {
        init();
    }

    private static void init() {
        executor = new ScheduledThreadPoolExecutor(4, new ThreadFactory() {
            final AtomicInteger counter = new AtomicInteger();

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "rhino-thread-" + counter.incrementAndGet());
                thread.setDaemon(true);
                return thread;
            }
        });

        executor.execute(new Runnable() {
            @Override
            public void run() {
                int port = -1;
                executor.execute(new HeartbeatTask(executor, port));
            }
        });

        RhinoHttpServer.getInstance();
    }

    public static void addHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            private OfflineService offlineService = new OfflineService();
            @Override
            public void run() {
                try {
                    //关闭资源隔离中的线程池
                    ThreadPool.Factory.shutDownThreadPools();
                    offlineService.offline();

                    RhinoHttpServer httpServer = RhinoHttpServer.getInstance();
                    if(httpServer != null){
                        httpServer.shutdown();
                    }
                } catch (Exception e) {
                    // ignore exception
                }
            }
        }));
    }

    /**
     * @param rhinoEntity
     */
    public static void report(RhinoEntity rhinoEntity) {

        Matcher matcher = keyPattern.matcher(rhinoEntity.getRhinoKey());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("rhino key只能由数字、英文字母、字符（_.-）构成" + rhinoEntity.getRhinoKey());
        }

        String msg = String.format("init %s with rhinoKey: %s", rhinoEntity.getType().toString(), rhinoEntity.getRhinoKey());
        logger.info(msg);

        if (rhinoEntity.getAppKey() != null) {
            executor.execute(new ReportTask(rhinoEntity, executor));
        }
    }

    /**
     * 上报开关信息
     *
     * @param switchEntity
     */
    public static void reportSwitch(SwitchEntity switchEntity) {
        String msg = String.format("init switch '%s' with value: %s, appKey: %s", switchEntity.getKey(), switchEntity.getValue(), switchEntity.getAppKey());
        logger.info(msg);
        executor.execute(new SwitchReportTask(switchEntity));
    }

    /**
     * 限流路径上报
     *
     * @param flowLimiterEntity
     */
    public static void reportOneLimiter(OneLimiterEntity flowLimiterEntity) {
        executor.execute(new OneLimiterReportTask(flowLimiterEntity));
    }

    /**
     * 限流告警上报
     *
     * @param flowLimiterEntity
     */
    public static void reportOneLimiterAlarm(OneLimiterAlarmEntity flowLimiterEntity) {
        executor.execute(new OneLimiterAlarmTask(flowLimiterEntity));
    }

    /**
     * @param notifyEvent
     */
    public static void notify(NotifyEvent notifyEvent) {
        executor.execute(new NotifyTask(notifyEvent));
    }
}