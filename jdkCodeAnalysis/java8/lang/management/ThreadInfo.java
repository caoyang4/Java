package java.lang.management;

import javax.management.openmbean.CompositeData;
import sun.management.ManagementFactoryHelper;
import sun.management.ThreadInfoCompositeData;
import static java.lang.Thread.State.*;

// 线程信息
public class ThreadInfo {
    private String       threadName;
    private long         threadId;
    private long         blockedTime;
    private long         blockedCount;
    private long         waitedTime;
    private long         waitedCount;
    private LockInfo     lock;
    private String       lockName;
    private long         lockOwnerId;
    private String       lockOwnerName;
    private boolean      inNative;
    private boolean      suspended;
    private Thread.State threadState;
    private StackTraceElement[] stackTrace;
    private MonitorInfo[]       lockedMonitors;
    private LockInfo[]          lockedSynchronizers;

    private static MonitorInfo[] EMPTY_MONITORS = new MonitorInfo[0];
    private static LockInfo[] EMPTY_SYNCS = new LockInfo[0];

    private ThreadInfo(Thread t, int state, Object lockObj, Thread lockOwner,
                       long blockedCount, long blockedTime,
                       long waitedCount, long waitedTime,
                       StackTraceElement[] stackTrace) {
        initialize(t, state, lockObj, lockOwner,
                   blockedCount, blockedTime,
                   waitedCount, waitedTime, stackTrace,
                   EMPTY_MONITORS, EMPTY_SYNCS);
    }

    private ThreadInfo(Thread t, int state, Object lockObj, Thread lockOwner,
                       long blockedCount, long blockedTime,
                       long waitedCount, long waitedTime,
                       StackTraceElement[] stackTrace,
                       Object[] monitors,
                       int[] stackDepths,
                       Object[] synchronizers) {
        int numMonitors = (monitors == null ? 0 : monitors.length);
        MonitorInfo[] lockedMonitors;
        if (numMonitors == 0) {
            lockedMonitors = EMPTY_MONITORS;
        } else {
            lockedMonitors = new MonitorInfo[numMonitors];
            for (int i = 0; i < numMonitors; i++) {
                Object lock = monitors[i];
                String className = lock.getClass().getName();
                int identityHashCode = System.identityHashCode(lock);
                int depth = stackDepths[i];
                StackTraceElement ste = (depth >= 0 ? stackTrace[depth] : null);
                lockedMonitors[i] = new MonitorInfo(className, identityHashCode, depth, ste);
            }
        }

        int numSyncs = (synchronizers == null ? 0 : synchronizers.length);
        LockInfo[] lockedSynchronizers;
        if (numSyncs == 0) {
            lockedSynchronizers = EMPTY_SYNCS;
        } else {
            lockedSynchronizers = new LockInfo[numSyncs];
            for (int i = 0; i < numSyncs; i++) {
                Object lock = synchronizers[i];
                String className = lock.getClass().getName();
                int identityHashCode = System.identityHashCode(lock);
                lockedSynchronizers[i] = new LockInfo(className,
                                                      identityHashCode);
            }
        }

        initialize(t, state, lockObj, lockOwner,
                   blockedCount, blockedTime,
                   waitedCount, waitedTime, stackTrace,
                   lockedMonitors, lockedSynchronizers);
    }

    private void initialize(Thread t, int state, Object lockObj, Thread lockOwner,
                            long blockedCount, long blockedTime,
                            long waitedCount, long waitedTime,
                            StackTraceElement[] stackTrace,
                            MonitorInfo[] lockedMonitors,
                            LockInfo[] lockedSynchronizers) {
        this.threadId = t.getId();
        this.threadName = t.getName();
        this.threadState = ManagementFactoryHelper.toThreadState(state);
        this.suspended = ManagementFactoryHelper.isThreadSuspended(state);
        this.inNative = ManagementFactoryHelper.isThreadRunningNative(state);
        this.blockedCount = blockedCount;
        this.blockedTime = blockedTime;
        this.waitedCount = waitedCount;
        this.waitedTime = waitedTime;

        if (lockObj == null) {
            this.lock = null;
            this.lockName = null;
        } else {
            this.lock = new LockInfo(lockObj);
            this.lockName =
                lock.getClassName() + '@' +
                    Integer.toHexString(lock.getIdentityHashCode());
        }
        if (lockOwner == null) {
            this.lockOwnerId = -1;
            this.lockOwnerName = null;
        } else {
            this.lockOwnerId = lockOwner.getId();
            this.lockOwnerName = lockOwner.getName();
        }
        if (stackTrace == null) {
            this.stackTrace = NO_STACK_TRACE;
        } else {
            this.stackTrace = stackTrace;
        }
        this.lockedMonitors = lockedMonitors;
        this.lockedSynchronizers = lockedSynchronizers;
    }

    /*
     * Constructs a <tt>ThreadInfo</tt> object from a
     * {@link CompositeData CompositeData}.
     */
    private ThreadInfo(CompositeData cd) {
        ThreadInfoCompositeData ticd = ThreadInfoCompositeData.getInstance(cd);

        threadId = ticd.threadId();
        threadName = ticd.threadName();
        blockedTime = ticd.blockedTime();
        blockedCount = ticd.blockedCount();
        waitedTime = ticd.waitedTime();
        waitedCount = ticd.waitedCount();
        lockName = ticd.lockName();
        lockOwnerId = ticd.lockOwnerId();
        lockOwnerName = ticd.lockOwnerName();
        threadState = ticd.threadState();
        suspended = ticd.suspended();
        inNative = ticd.inNative();
        stackTrace = ticd.stackTrace();

        // 6.0 attributes
        if (ticd.isCurrentVersion()) {
            lock = ticd.lockInfo();
            lockedMonitors = ticd.lockedMonitors();
            lockedSynchronizers = ticd.lockedSynchronizers();
        } else {
            // lockInfo is a new attribute added in 1.6 ThreadInfo
            // If cd is a 5.0 version, construct the LockInfo object
            //  from the lockName value.
            if (lockName != null) {
                String result[] = lockName.split("@");
                if (result.length == 2) {
                    int identityHashCode = Integer.parseInt(result[1], 16);
                    lock = new LockInfo(result[0], identityHashCode);
                } else {
                    assert result.length == 2;
                    lock = null;
                }
            } else {
                lock = null;
            }
            lockedMonitors = EMPTY_MONITORS;
            lockedSynchronizers = EMPTY_SYNCS;
        }
    }

    public long getThreadId() {
        return threadId;
    }

    public String getThreadName() {
        return threadName;
    }

    public Thread.State getThreadState() {
         return threadState;
    }

    public long getBlockedTime() {
        return blockedTime;
    }

    public long getBlockedCount() {
        return blockedCount;
    }

    public long getWaitedTime() {
        return waitedTime;
    }

    public long getWaitedCount() {
        return waitedCount;
    }

    public LockInfo getLockInfo() {
        return lock;
    }

    public String getLockName() {
        return lockName;
    }

    public long getLockOwnerId() {
        return lockOwnerId;
    }

    public String getLockOwnerName() {
        return lockOwnerName;
    }

    public StackTraceElement[] getStackTrace() {
        return stackTrace;
    }

    public boolean isSuspended() {
         return suspended;
    }

    public boolean isInNative() {
         return inNative;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("\"" + getThreadName() + "\"" + " Id=" + getThreadId() + " " + getThreadState());
        if (getLockName() != null) {
            sb.append(" on " + getLockName());
        }
        if (getLockOwnerName() != null) {
            sb.append(" owned by \"" + getLockOwnerName() + "\" Id=" + getLockOwnerId());
        }
        if (isSuspended()) {
            sb.append(" (suspended)");
        }
        if (isInNative()) {
            sb.append(" (in native)");
        }
        sb.append('\n');
        int i = 0;
        for (; i < stackTrace.length && i < MAX_FRAMES; i++) {
            StackTraceElement ste = stackTrace[i];
            sb.append("\tat " + ste.toString());
            sb.append('\n');
            if (i == 0 && getLockInfo() != null) {
                Thread.State ts = getThreadState();
                switch (ts) {
                    case BLOCKED:
                        sb.append("\t-  blocked on " + getLockInfo());
                        sb.append('\n');
                        break;
                    case WAITING:
                        sb.append("\t-  waiting on " + getLockInfo());
                        sb.append('\n');
                        break;
                    case TIMED_WAITING:
                        sb.append("\t-  waiting on " + getLockInfo());
                        sb.append('\n');
                        break;
                    default:
                }
            }

            for (MonitorInfo mi : lockedMonitors) {
                if (mi.getLockedStackDepth() == i) {
                    sb.append("\t-  locked " + mi);
                    sb.append('\n');
                }
            }
       }
       if (i < stackTrace.length) {
           sb.append("\t...");
           sb.append('\n');
       }

       LockInfo[] locks = getLockedSynchronizers();
       if (locks.length > 0) {
           sb.append("\n\tNumber of locked synchronizers = " + locks.length);
           sb.append('\n');
           for (LockInfo li : locks) {
               sb.append("\t- " + li);
               sb.append('\n');
           }
       }
       sb.append('\n');
       return sb.toString();
    }
    private static final int MAX_FRAMES = 8;

    public static ThreadInfo from(CompositeData cd) {
        if (cd == null) {
            return null;
        }

        if (cd instanceof ThreadInfoCompositeData) {
            return ((ThreadInfoCompositeData) cd).getThreadInfo();
        } else {
            return new ThreadInfo(cd);
        }
    }

    public MonitorInfo[] getLockedMonitors() {
        return lockedMonitors;
    }

    public LockInfo[] getLockedSynchronizers() {
        return lockedSynchronizers;
    }

    private static final StackTraceElement[] NO_STACK_TRACE =
        new StackTraceElement[0];
}
