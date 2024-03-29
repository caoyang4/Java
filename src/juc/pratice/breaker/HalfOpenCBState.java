package src.juc.pratice.breaker;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * 熔断器-半开状态
 *
 * @author caoyang
 * @date 2023/07/05
 */
public class HalfOpenCBState implements CBState {

    /**
     * 进入当前状态的初始化时间
     */
    private final long stateTime = System.currentTimeMillis();

    /**
     * 半开状态，失败计数器
     */
    private final AtomicInteger failNum = new AtomicInteger(0);

    /**
     * 半开状态，允许通过的计数器
     */
    private final AtomicInteger passNum = new AtomicInteger(0);


    public String getStateName() {
        // 获取当前状态名称
        return this.getClass().getSimpleName();
    }

    public void checkAndSwitchState(AbstractCircuitBreaker cb) {
        // 判断半开时间是否结束
        long idleTime = Long.parseLong(cb.thresholdPassRateForHalfOpen.split("/")[1]) * 1000L;
        long now = System.currentTimeMillis();
        if (stateTime + idleTime <= now){
            // 如果半开状态已结束，失败次数是否超过了阀值
            int maxFailNum = cb.thresholdFailNumForHalfOpen;
            if (failNum.get() >= maxFailNum){
                // 失败超过阀值，认为服务没有恢复，重新进入熔断打开状态
                cb.setState(new OpenCBState());
            }else {
                // 没超过，认为服务恢复，进入熔断关闭状态
                cb.setState(new CloseCBState());
            }
        }
    }

    public boolean canPassCheck(AbstractCircuitBreaker cb) {
        // 检查是否切换状态
        checkAndSwitchState(cb);

        // 超过了阀值，不再放量
        int maxPassNum = Integer.parseInt(cb.thresholdPassRateForHalfOpen.split("/")[0]);
        if (passNum.get() > maxPassNum){
            return false;
        }
        // 检测是否超过了阀值
        return passNum.incrementAndGet() <= maxPassNum;
    }

    public void countFailNum(AbstractCircuitBreaker cb) {
        // 失败计数
        failNum.incrementAndGet();

        // 检查是否切换状态
        checkAndSwitchState(cb);
    }
}