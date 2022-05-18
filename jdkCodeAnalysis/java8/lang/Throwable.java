package java.lang;

import java.io.*;
import java.util.*;

/**
 * java异常体系
 */
public class Throwable implements Serializable {
    private static final long serialVersionUID = -3042686055658047285L;

    private transient Object backtrace;

    private String detailMessage;

    private static class SentinelHolder {
        public static final StackTraceElement STACK_TRACE_ELEMENT_SENTINEL =
            new StackTraceElement("", "", null, Integer.MIN_VALUE);

        public static final StackTraceElement[] STACK_TRACE_SENTINEL =
            new StackTraceElement[] {STACK_TRACE_ELEMENT_SENTINEL};
    }

    private static final StackTraceElement[] UNASSIGNED_STACK = new StackTraceElement[0];

    /*
     * 1) The fields are initialized to a non-null sentinel value
     * which indicates the value has logically not been set.
     *
     * 2) Writing a null to the field indicates further writes are forbidden
     *
     * 3) The sentinel value may be replaced with another non-null value.
     */

    private Throwable cause = this;

    private StackTraceElement[] stackTrace = UNASSIGNED_STACK;

    // Setting this static field introduces an acceptable
    // initialization dependency on a few java.util classes.
    private static final List<Throwable> SUPPRESSED_SENTINEL =
        Collections.unmodifiableList(new ArrayList<Throwable>(0));

    private List<Throwable> suppressedExceptions = SUPPRESSED_SENTINEL;

    private static final String NULL_CAUSE_MESSAGE = "Cannot suppress a null exception.";

    private static final String SELF_SUPPRESSION_MESSAGE = "Self-suppression not permitted";

    private static final String CAUSE_CAPTION = "Caused by: ";

    private static final String SUPPRESSED_CAPTION = "Suppressed: ";

    public Throwable() {
        fillInStackTrace();
    }

    public Throwable(String message) {
        fillInStackTrace();
        detailMessage = message;
    }

    public Throwable(String message, Throwable cause) {
        fillInStackTrace();
        detailMessage = message;
        this.cause = cause;
    }

    public Throwable(Throwable cause) {
        fillInStackTrace();
        detailMessage = (cause==null ? null : cause.toString());
        this.cause = cause;
    }

    protected Throwable(String message, Throwable cause,
                        boolean enableSuppression,
                        boolean writableStackTrace) {
        if (writableStackTrace) {
            fillInStackTrace();
        } else {
            stackTrace = null;
        }
        detailMessage = message;
        this.cause = cause;
        if (!enableSuppression)
            suppressedExceptions = null;
    }

    public String getMessage() {
        return detailMessage;
    }

    public String getLocalizedMessage() {
        return getMessage();
    }

    public synchronized Throwable getCause() {
        return (cause==this ? null : cause);
    }

    public synchronized Throwable initCause(Throwable cause) {
        if (this.cause != this)
            throw new IllegalStateException("Can't overwrite cause with " + Objects.toString(cause, "a null"), this);
        if (cause == this)
            throw new IllegalArgumentException("Self-causation not permitted", this);
        this.cause = cause;
        return this;
    }

    public String toString() {
        String s = getClass().getName();
        String message = getLocalizedMessage();
        return (message != null) ? (s + ": " + message) : s;
    }

    public void printStackTrace() {
        printStackTrace(System.err);
    }

    public void printStackTrace(PrintStream s) {
        printStackTrace(new WrappedPrintStream(s));
    }

    private void printStackTrace(PrintStreamOrWriter s) {
        // Guard against malicious overrides of Throwable.equals by
        // using a Set with identity equality semantics.
        Set<Throwable> dejaVu =
            Collections.newSetFromMap(new IdentityHashMap<Throwable, Boolean>());
        dejaVu.add(this);

        synchronized (s.lock()) {
            // Print our stack trace
            s.println(this);
            StackTraceElement[] trace = getOurStackTrace();
            for (StackTraceElement traceElement : trace)
                s.println("\tat " + traceElement);

            // Print suppressed exceptions, if any
            for (Throwable se : getSuppressed())
                se.printEnclosedStackTrace(s, trace, SUPPRESSED_CAPTION, "\t", dejaVu);

            // Print cause, if any
            Throwable ourCause = getCause();
            if (ourCause != null)
                ourCause.printEnclosedStackTrace(s, trace, CAUSE_CAPTION, "", dejaVu);
        }
    }

    private void printEnclosedStackTrace(PrintStreamOrWriter s,
                                         StackTraceElement[] enclosingTrace,
                                         String caption,
                                         String prefix,
                                         Set<Throwable> dejaVu) {
        assert Thread.holdsLock(s.lock());
        if (dejaVu.contains(this)) {
            s.println(prefix + caption + "[CIRCULAR REFERENCE: " + this + "]");
        } else {
            dejaVu.add(this);
            // Compute number of frames in common between this and enclosing trace
            StackTraceElement[] trace = getOurStackTrace();
            int m = trace.length - 1;
            int n = enclosingTrace.length - 1;
            while (m >= 0 && n >=0 && trace[m].equals(enclosingTrace[n])) {
                m--; n--;
            }
            int framesInCommon = trace.length - 1 - m;

            // Print our stack trace
            s.println(prefix + caption + this);
            for (int i = 0; i <= m; i++)
                s.println(prefix + "\tat " + trace[i]);
            if (framesInCommon != 0)
                s.println(prefix + "\t... " + framesInCommon + " more");

            // Print suppressed exceptions, if any
            for (Throwable se : getSuppressed())
                se.printEnclosedStackTrace(s, trace, SUPPRESSED_CAPTION,
                                           prefix +"\t", dejaVu);

            // Print cause, if any
            Throwable ourCause = getCause();
            if (ourCause != null)
                ourCause.printEnclosedStackTrace(s, trace, CAUSE_CAPTION, prefix, dejaVu);
        }
    }

    public void printStackTrace(PrintWriter s) {
        printStackTrace(new WrappedPrintWriter(s));
    }

    private abstract static class PrintStreamOrWriter {
        abstract Object lock();

        abstract void println(Object o);
    }

    private static class WrappedPrintStream extends PrintStreamOrWriter {
        private final PrintStream printStream;

        WrappedPrintStream(PrintStream printStream) {
            this.printStream = printStream;
        }

        Object lock() {
            return printStream;
        }

        void println(Object o) {
            printStream.println(o);
        }
    }

    private static class WrappedPrintWriter extends PrintStreamOrWriter {
        private final PrintWriter printWriter;

        WrappedPrintWriter(PrintWriter printWriter) {
            this.printWriter = printWriter;
        }

        Object lock() {
            return printWriter;
        }

        void println(Object o) {
            printWriter.println(o);
        }
    }

    public synchronized Throwable fillInStackTrace() {
        if (stackTrace != null ||
            backtrace != null /* Out of protocol state */ ) {
            fillInStackTrace(0);
            stackTrace = UNASSIGNED_STACK;
        }
        return this;
    }

    private native Throwable fillInStackTrace(int dummy);

    public StackTraceElement[] getStackTrace() {
        return getOurStackTrace().clone();
    }

    private synchronized StackTraceElement[] getOurStackTrace() {
        // Initialize stack trace field with information from
        // backtrace if this is the first call to this method
        if (stackTrace == UNASSIGNED_STACK ||
            (stackTrace == null && backtrace != null) /* Out of protocol state */) {
            int depth = getStackTraceDepth();
            stackTrace = new StackTraceElement[depth];
            for (int i=0; i < depth; i++)
                stackTrace[i] = getStackTraceElement(i);
        } else if (stackTrace == null) {
            return UNASSIGNED_STACK;
        }
        return stackTrace;
    }

    public void setStackTrace(StackTraceElement[] stackTrace) {
        // Validate argument
        StackTraceElement[] defensiveCopy = stackTrace.clone();
        for (int i = 0; i < defensiveCopy.length; i++) {
            if (defensiveCopy[i] == null)
                throw new NullPointerException("stackTrace[" + i + "]");
        }

        synchronized (this) {
            if (this.stackTrace == null && // Immutable stack
                backtrace == null) // Test for out of protocol state
                return;
            this.stackTrace = defensiveCopy;
        }
    }

    native int getStackTraceDepth();

    native StackTraceElement getStackTraceElement(int index);

    private void readObject(ObjectInputStream s)
        throws IOException, ClassNotFoundException {
        s.defaultReadObject();     // read in all fields

        // Set suppressed exceptions and stack trace elements fields
        // to marker values until the contents from the serial stream
        // are validated.
        List<Throwable> candidateSuppressedExceptions = suppressedExceptions;
        suppressedExceptions = SUPPRESSED_SENTINEL;

        StackTraceElement[] candidateStackTrace = stackTrace;
        stackTrace = UNASSIGNED_STACK.clone();

        if (candidateSuppressedExceptions != null) {
            int suppressedSize = validateSuppressedExceptionsList(candidateSuppressedExceptions);
            if (suppressedSize > 0) { // Copy valid Throwables to new list
                List<Throwable> suppList  = new ArrayList<Throwable>(Math.min(100, suppressedSize));

                for (Throwable t : candidateSuppressedExceptions) {
                    // Enforce constraints on suppressed exceptions in
                    // case of corrupt or malicious stream.
                    if (t == null)
                        throw new NullPointerException(NULL_CAUSE_MESSAGE);
                    if (t == this)
                        throw new IllegalArgumentException(SELF_SUPPRESSION_MESSAGE);
                    suppList.add(t);
                }
                // If there are any invalid suppressed exceptions,
                // implicitly use the sentinel value assigned earlier.
                suppressedExceptions = suppList;
            }
        } else {
            suppressedExceptions = null;
        }

        if (candidateStackTrace != null) {
            // Work from a clone of the candidateStackTrace to ensure
            // consistency of checks.
            candidateStackTrace = candidateStackTrace.clone();
            if (candidateStackTrace.length >= 1) {
                if (candidateStackTrace.length == 1 &&
                        // Check for the marker of an immutable stack trace
                        SentinelHolder.STACK_TRACE_ELEMENT_SENTINEL.equals(candidateStackTrace[0])) {
                    stackTrace = null;
                } else { // Verify stack trace elements are non-null.
                    for (StackTraceElement ste : candidateStackTrace) {
                        if (ste == null)
                            throw new NullPointerException("null StackTraceElement in serial stream.");
                    }
                    stackTrace = candidateStackTrace;
                }
            }
        }
        // A null stackTrace field in the serial form can result from
        // an exception serialized without that field in older JDK
        // releases; treat such exceptions as having empty stack
        // traces by leaving stackTrace assigned to a clone of
        // UNASSIGNED_STACK.
    }

    private int validateSuppressedExceptionsList(List<Throwable> deserSuppressedExceptions)
        throws IOException {
        if (Object.class.getClassLoader() != deserSuppressedExceptions.getClass().getClassLoader()) {
            throw new StreamCorruptedException("List implementation not on the bootclasspath.");
        } else {
            int size = deserSuppressedExceptions.size();
            if (size < 0) {
                throw new StreamCorruptedException("Negative list size reported.");
            }
            return size;
        }
    }

    private synchronized void writeObject(ObjectOutputStream s)
        throws IOException {
        // Ensure that the stackTrace field is initialized to a
        // non-null value, if appropriate.  As of JDK 7, a null stack
        // trace field is a valid value indicating the stack trace
        // should not be set.
        getOurStackTrace();

        StackTraceElement[] oldStackTrace = stackTrace;
        try {
            if (stackTrace == null)
                stackTrace = SentinelHolder.STACK_TRACE_SENTINEL;
            s.defaultWriteObject();
        } finally {
            stackTrace = oldStackTrace;
        }
    }

    public final synchronized void addSuppressed(Throwable exception) {
        if (exception == this)
            throw new IllegalArgumentException(SELF_SUPPRESSION_MESSAGE, exception);

        if (exception == null)
            throw new NullPointerException(NULL_CAUSE_MESSAGE);

        if (suppressedExceptions == null) // Suppressed exceptions not recorded
            return;

        if (suppressedExceptions == SUPPRESSED_SENTINEL)
            suppressedExceptions = new ArrayList<>(1);

        suppressedExceptions.add(exception);
    }

    private static final Throwable[] EMPTY_THROWABLE_ARRAY = new Throwable[0];

    public final synchronized Throwable[] getSuppressed() {
        if (suppressedExceptions == SUPPRESSED_SENTINEL ||
            suppressedExceptions == null)
            return EMPTY_THROWABLE_ARRAY;
        else
            return suppressedExceptions.toArray(EMPTY_THROWABLE_ARRAY);
    }
}
