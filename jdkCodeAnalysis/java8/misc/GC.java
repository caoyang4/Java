package sun.misc;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.SortedSet;
import java.util.TreeSet;


public class GC {

    private GC() { }            /* To prevent instantiation */


    /* Latency-target value indicating that there's no active target
     */
    private static final long NO_TARGET = Long.MAX_VALUE;

    /* The current latency target, or NO_TARGET if there is no target
     */
    private static long latencyTarget = NO_TARGET;

    /* The daemon thread that implements the latency-target mechanism,
     * or null if there is presently no daemon thread
     */
    private static Thread daemon = null;

    /* The lock object for the latencyTarget and daemon fields.  The daemon
     * thread, if it exists, waits on this lock for notification that the
     * latency target has changed.
     */
    private static class LatencyLock extends Object { };
    private static Object lock = new LatencyLock();


    public static native long maxObjectInspectionAge();


    private static class Daemon extends Thread {

        public void run() {
            for (;;) {
                long l;
                synchronized (lock) {

                    l = latencyTarget;
                    if (l == NO_TARGET) {
                        /* No latency target, so exit */
                        GC.daemon = null;
                        return;
                    }

                    long d = maxObjectInspectionAge();
                    if (d >= l) {
                        /* Do a full collection.  There is a remote possibility
                         * that a full collection will occurr between the time
                         * we sample the inspection age and the time the GC
                         * actually starts, but this is sufficiently unlikely
                         * that it doesn't seem worth the more expensive JVM
                         * interface that would be required.
                         */
                        System.gc();
                        d = 0;
                    }

                    /* Wait for the latency period to expire,
                     * or for notification that the period has changed
                     */
                    try {
                        lock.wait(l - d);
                    } catch (InterruptedException x) {
                        continue;
                    }
                }
            }
        }

        private Daemon(ThreadGroup tg) {
            super(tg, "GC Daemon");
        }

        /* Create a new daemon thread in the root thread group */
        public static void create() {
            PrivilegedAction<Void> pa = new PrivilegedAction<Void>() {
                public Void run() {
                    ThreadGroup tg = Thread.currentThread().getThreadGroup();
                    for (ThreadGroup tgn = tg;
                         tgn != null;
                         tg = tgn, tgn = tg.getParent());
                    Daemon d = new Daemon(tg);
                    // 守护线程
                    d.setDaemon(true);
                    d.setPriority(Thread.MIN_PRIORITY + 1);
                    d.start();
                    GC.daemon = d;
                    return null;
                }};
            AccessController.doPrivileged(pa);
        }

    }


    /* Sets the latency target to the given value.
     * Must be invoked while holding the lock.
     */
    private static void setLatencyTarget(long ms) {
        latencyTarget = ms;
        if (daemon == null) {
            /* Create a new daemon thread */
            Daemon.create();
        } else {
            /* Notify the existing daemon thread
             * that the lateency target has changed
             */
            lock.notify();
        }
    }


    public static class LatencyRequest implements Comparable<LatencyRequest> {

        /* Instance counter, used to generate unique identifers */
        private static long counter = 0;

        /* Sorted set of active latency requests */
        private static SortedSet<LatencyRequest> requests = null;

        /* Examine the request set and reset the latency target if necessary.
         * Must be invoked while holding the lock.
         */
        private static void adjustLatencyIfNeeded() {
            if ((requests == null) || requests.isEmpty()) {
                if (latencyTarget != NO_TARGET) {
                    setLatencyTarget(NO_TARGET);
                }
            } else {
                LatencyRequest r = requests.first();
                if (r.latency != latencyTarget) {
                    setLatencyTarget(r.latency);
                }
            }
        }

        /* The requested latency, or NO_TARGET
         * if this request has been cancelled
         */
        private long latency;

        /* Unique identifier for this request */
        private long id;

        private LatencyRequest(long ms) {
            if (ms <= 0) {
                throw new IllegalArgumentException("Non-positive latency: " + ms);
            }
            this.latency = ms;
            synchronized (lock) {
                this.id = ++counter;
                if (requests == null) {
                    requests = new TreeSet<LatencyRequest>();
                }
                requests.add(this);
                adjustLatencyIfNeeded();
            }
        }

        public void cancel() {
            synchronized (lock) {
                if (this.latency == NO_TARGET) {
                    throw new IllegalStateException("Request already" + " cancelled");
                }
                if (!requests.remove(this)) {
                    throw new InternalError("Latency request " + this + " not found");
                }
                if (requests.isEmpty()) requests = null;
                this.latency = NO_TARGET;
                adjustLatencyIfNeeded();
            }
        }

        public int compareTo(LatencyRequest r) {
            long d = this.latency - r.latency;
            if (d == 0) d = this.id - r.id;
            return (d < 0) ? -1 : ((d > 0) ? +1 : 0);
        }

        public String toString() {
            return (LatencyRequest.class.getName() + "[" + latency + "," + id + "]");
        }

    }


    public static LatencyRequest requestLatency(long latency) {
        return new LatencyRequest(latency);
    }


    public static long currentLatencyTarget() {
        long t = latencyTarget;
        return (t == NO_TARGET) ? 0 : t;
    }

}
