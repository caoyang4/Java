package src.juc.pratice.breaker;


/**
 * 熔断器接口
 *
 * @author caoyang
 * @date 2023/07/05
 */
public interface CircuitBreaker {
    /**
     * 重置熔断器
     */
    void reset();

    /**
     * 是否允许通过熔断器
     */
    boolean canPassCheck();

    /**
     * 统计失败次数
     */
    void countFailNum();

}
