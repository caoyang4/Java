package src.rhino.threadpool;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zhanjun on 2018/8/1.
 */
public class  RhinoRequestContext {

    private static ThreadLocal<RhinoRequestContext> requestVariables = new ThreadLocal<>();

    /*
     * This ConcurrentHashMap should not be made publicly accessible. It is the state of RequestVariables for a given RequestContext.
     *
     * Only HystrixRequestVariable has a reason to be accessing this field.
     */
    ConcurrentHashMap<RhinoThreadLocal<?>, RhinoThreadLocal.LazyInitializer<?>> state = new ConcurrentHashMap();

    /**
     * Call this at the beginning of each request (from parent thread)
     * to initialize the underlying context so that {@link RhinoThreadLocal} can be used on any children threads and be accessible from
     * the parent thread.
     * <p>
     * <b>NOTE: If this method is called then <code>shutdown()</code> must also be called or a memory leak will occur.</b>
     * <p>
     * See class header JavaDoc for example Servlet Filter implementation that initializes and shuts down the context.
     */
    public static RhinoRequestContext initializeContext() {
        RhinoRequestContext context = requestVariables.get();
        if (context == null) {
            context = new RhinoRequestContext();
            requestVariables.set(context);
        }
        return context;
    }

    /**
     *
     * @return
     */
    public static RhinoRequestContext getContextForCurrentThread() {
        RhinoRequestContext context = requestVariables.get();
        if (context != null && context.state != null) {
            // context.state can be null when context is not null
            // if a thread is being re-used and held a context previously, the context was shut down
            // but the thread was not cleared
            return context;
        } else {
            return null;
        }
    }

    /**
     *
     * @param context
     */
    public static void setContextOnCurrentThread(RhinoRequestContext context) {
        requestVariables.set(context);
    }
}
