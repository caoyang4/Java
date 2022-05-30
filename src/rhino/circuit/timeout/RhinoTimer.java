package src.rhino.circuit.timeout;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import src.rhino.log.Logger;
import src.rhino.log.LoggerFactory;
import src.rhino.util.PlatformSpecific;

/**
 * @author zhanjun on 2017/6/6.
 */
public class RhinoTimer {

    private static final Logger logger = LoggerFactory.getLogger(RhinoTimer.class);

    private static RhinoTimer INSTANCE = new RhinoTimer();

    private RhinoTimer() {
        // private to prevent public instantiation
    }

    /**
     * Retrieve the global instance.
     */
    public static RhinoTimer getInstance() {
        return INSTANCE;
    }

    private AtomicReference<ScheduledExecutor> executor = new AtomicReference<>();

    /**
     * Add a {@link TimerListener} that will be executed until it is garbage collected or removed by clearing the returned {@link Reference}.
     * <p>
     * NOTE: It is the responsibility of code that adds a listener via this method to clear this listener when completed.
     * <p>
     * <blockquote>
     *
     * <pre> {@code
     * // add a TimerListener
     * Reference<TimerListener> listener = RhinoTimer.getInstance().addTimerListener(listenerImpl);
     *
     * // sometime later, often in a thread shutdown, request cleanup, servlet filter or something similar the listener must be shutdown via the clear() method
     * listener.clear();
     * }</pre>
     * </blockquote>
     *
     *
     * @param listener
     *            TimerListener implementation that will be triggered according to its <code>getIntervalTimeInMilliseconds()</code> method implementation.
     * @return reference to the TimerListener that allows cleanup via the <code>clear()</code> method
     */
    public Reference<TimerListener> addTimerListener(final TimerListener listener) {
        startThreadIfNeeded();
        // add the listener

        Runnable r = new Runnable() {

            @Override
            public void run() {
                try {
                    listener.tick();
                } catch (Exception e) {
                    logger.error("Failed while ticking TimerListener", e);
                }
            }
        };

        ScheduledFuture<?> f = executor.get().getThreadPool().scheduleAtFixedRate(r, listener.getIntervalTimeInMilliseconds(), listener.getIntervalTimeInMilliseconds(), TimeUnit.MILLISECONDS);
        return new TimerReference(listener, f);
    }

    private static class TimerReference extends SoftReference<TimerListener> {

        private final ScheduledFuture<?> f;

        TimerReference(TimerListener referent, ScheduledFuture<?> f) {
            super(referent);
            this.f = f;
        }

        @Override
        public void clear() {
            super.clear();
            // stop this ScheduledFuture from any further executions
            f.cancel(false);
        }

    }

    /**
     * Since we allow resetting the timer (shutting down the thread) we need to lazily re-start it if it starts being used again.
     * <p>
     * This does the lazy initialization and start of the thread in a thread-safe manner while having little cost the rest of the time.
     */
    protected void startThreadIfNeeded() {
        // create and start thread if one doesn't exist
        while (executor.get() == null || ! executor.get().isInitialized()) {
            if (executor.compareAndSet(null, new ScheduledExecutor())) {
                // initialize the executor that we 'won' setting
                executor.get().initialize();
            }
        }
    }

    static class ScheduledExecutor {
        volatile ScheduledThreadPoolExecutor executor;
        private volatile boolean initialized;

        /**
         * We want this only done once when created in compareAndSet so use an initialize method
         */
        public void initialize() {

            int coreSize = 8;
            ThreadFactory threadFactory;
            if (!PlatformSpecific.isAppEngineStandardEnvironment()) {
                threadFactory = new ThreadFactory() {
                    final AtomicInteger counter = new AtomicInteger();

                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r, "Rhino-Timer-" + counter.incrementAndGet());
                        thread.setDaemon(true);
                        return thread;
                    }

                };
            } else {
                threadFactory = PlatformSpecific.getAppEngineThreadFactory();
            }

            executor = new ScheduledThreadPoolExecutor(coreSize, threadFactory);
            initialized = true;
        }

        public ScheduledThreadPoolExecutor getThreadPool() {
            return executor;
        }

        public boolean isInitialized() {
            return initialized;
        }
    }
}
