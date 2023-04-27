package src.juc.pratice.breaker;

/**
 * @author caoyang
 * @create 2023-04-26 19:30
 */
public class CircuitBreaker {
    private final int MAX_FAILURES = 3; // 最大故障次数
    private final long TIMEOUT = 5000; // 超时时间，5秒
    private long lastFailureTime = 0; // 上一次故障时间
    private int consecutiveFailures = 0; // 连续故障次数

    public boolean allowRequest() {
        if (System.currentTimeMillis() - lastFailureTime < TIMEOUT) {
            return false; // 如果上一次故障时间距离现在还没有超过超时时间，熔断器拒绝请求。
        } else if (consecutiveFailures >= MAX_FAILURES) {
            return false; // 如果故障次数超过了设定的最大值，熔断器拒绝请求。
        } else {
            return true; // 允许请求通过。
        }
    }

    public void recordSuccess() {
        consecutiveFailures = 0; // 连续故障次数清零。
    }

    public void recordFailure() {
        // 根据故障时间和连续故障次数判断是否需要打开熔断器。
        if (System.currentTimeMillis() - lastFailureTime > TIMEOUT) {
            consecutiveFailures = 1;
        } else {
            consecutiveFailures++;
        }
        lastFailureTime = System.currentTimeMillis();
    }
}
