package src.rhino.onelimiter.cluster;

/**
 * @author Feng
 * @date 2020-06-17
 */
public final class TokenResultStatus {

    /**
     * Server or client unexpected failure (due to transport or serialization failure).
     */
    public static final int FAIL = -1;
    /**
     * Token acquired.
     */
    public static final int OK = 0;
    /**
     * Token acquire succeed, but reached warn level,
     */
    public static final int WARN = 1;
    /**
     * Token acquire failed (blocked).
     */
    public static final int BLOCKED = 2;
    /**
     * Token acquire failed (no rule exists).
     */
    public static final int NO_RULE_EXISTS = 3;

    private TokenResultStatus() {}
}
