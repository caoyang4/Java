package java.lang.management;

import javax.management.openmbean.CompositeData;
import sun.management.MonitorInfoCompositeData;

// 监视器信息
public class MonitorInfo extends LockInfo {

    private int    stackDepth;
    private StackTraceElement stackFrame;

    public MonitorInfo(String className, int identityHashCode, int stackDepth, StackTraceElement stackFrame) {
        super(className, identityHashCode);
        if (stackDepth >= 0 && stackFrame == null) {
            throw new IllegalArgumentException("Parameter stackDepth is " + stackDepth + " but stackFrame is null");
        }
        if (stackDepth < 0 && stackFrame != null) {
            throw new IllegalArgumentException("Parameter stackDepth is " + stackDepth + " but stackFrame is not null");
        }
        this.stackDepth = stackDepth;
        this.stackFrame = stackFrame;
    }

    public int getLockedStackDepth() {
        return stackDepth;
    }

    public StackTraceElement getLockedStackFrame() {
        return stackFrame;
    }

    public static MonitorInfo from(CompositeData cd) {
        if (cd == null) {
            return null;
        }

        if (cd instanceof MonitorInfoCompositeData) {
            return ((MonitorInfoCompositeData) cd).getMonitorInfo();
        } else {
            MonitorInfoCompositeData.validateCompositeData(cd);
            String className = MonitorInfoCompositeData.getClassName(cd);
            int identityHashCode = MonitorInfoCompositeData.getIdentityHashCode(cd);
            int stackDepth = MonitorInfoCompositeData.getLockedStackDepth(cd);
            StackTraceElement stackFrame = MonitorInfoCompositeData.getLockedStackFrame(cd);
            return new MonitorInfo(className, identityHashCode, stackDepth, stackFrame);
        }
    }

}
