package src.rhino.circuit;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author zhanjun
 * @date 2019/1/25
 */
public class CircuitBreakerRuntime {

    /**
     * 熔断器状态
     * 1、CLOSE
     * 2、OPEN
     * 3、HALF_OPEN
     * <p>
     * 状态之间的转换
     * CLOSE ----> OPEN [失败率过高]
     * OPEN  ----> HALF_OPEN [试探成功]
     * HALF_OPEN ----> OPEN  [试探失败]
     * HALF_OPEN ----> CLOSE [恢复]
     */
    private AtomicReference<CircuitBreaker.Status> circuitStatus = new AtomicReference<>(CircuitBreaker.Status.CLOSE);

    /**
     * 上次进行试探时间点
     */
    private AtomicLong lastTestPoint = new AtomicLong();

    /**
     * CLOSE ----> OPEN
     * 当前接口失败率过高，开始熔断
     *
     * @return
     */
    public boolean tryOpenCircuit() {
        boolean result = circuitStatus.compareAndSet(CircuitBreaker.Status.CLOSE, CircuitBreaker.Status.OPEN);
        if (result) {
            lastTestPoint.set(System.currentTimeMillis());
        }
        return result;
    }

    /**
     * OPEN ----> HALF_OPEN
     * 试探成功，开始恢复正常请求
     */
    public void setTestSuccess() {
        circuitStatus.compareAndSet(CircuitBreaker.Status.OPEN, CircuitBreaker.Status.HALF_OPEN);
        lastTestPoint.set(System.currentTimeMillis());
    }

    /**
     * HALF_OPEN ----> OPEN
     * 恢复过程中失败率还是过高，重新试探
     */
    public void setTestFailed() {
        if (circuitStatus.compareAndSet(CircuitBreaker.Status.HALF_OPEN, CircuitBreaker.Status.OPEN)) {
            lastTestPoint.set(System.currentTimeMillis());
        }
    }

    /**
     * HALF_OPEN ----> CLOSE
     * 当前接口失败率正常
     *
     * @return
     */
    public boolean tryCloseCircuit() {
        boolean result = circuitStatus.compareAndSet(CircuitBreaker.Status.HALF_OPEN, CircuitBreaker.Status.CLOSE);
        if (result) {
            lastTestPoint.set(0);
        }
        return result;
    }

    /**
     * 确保一个时间窗口只有一个线程能够进入
     *
     * @param sleepWindowInMilliseconds
     * @return
     */
    public boolean allowHeartbeat(long sleepWindowInMilliseconds) {
        long lastTestedTimeMilliseconds = lastTestPoint.get();
        // 防止熔断开启时，lastTryPoint还未赋值，导致提前试探
        if (lastTestedTimeMilliseconds == 0) {
            return false;
        }
        long nextTime = lastTestedTimeMilliseconds + sleepWindowInMilliseconds;
        long currentTime = System.currentTimeMillis();
        return currentTime >= nextTime && lastTestPoint.compareAndSet(lastTestedTimeMilliseconds, currentTime);
    }

    /**
     * 熔断器状态
     *
     * @return
     */
    public CircuitBreaker.Status getCircuitStatus() {
        return circuitStatus.get();
    }
}
