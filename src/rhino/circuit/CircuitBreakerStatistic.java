package src.rhino.circuit;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by zhanjun on 2019/1/25.
 */
public class CircuitBreakerStatistic {

    /**
     * 自动熔断统计
     */
    private Entity autoOpenEntity = new Entity();

    /**
     * 强制熔断统计
     */
    private Entity forceOpenEntity = new Entity();


    /**
     *
     * @param forceOpen
     */
    public void markOpened(boolean forceOpen) {
        if (forceOpen) {
            forceOpenEntity.markOpened();
        } else {
            autoOpenEntity.markOpened();
        }
    }

    /**
     *
     * @param forceOpen
     */
    public void markClosed(boolean forceOpen) {
        if (forceOpen) {
            forceOpenEntity.markClosed();
        } else {
            autoOpenEntity.markClosed();
        }
    }

    /**
     *
     * @param forceOpen
     * @return
     */
    public long getCircuitDurationInSecond(boolean forceOpen) {
        if (forceOpen) {
            return forceOpenEntity.getCircuitDurationInSecond();
        } else {
            return autoOpenEntity.getCircuitDurationInSecond();
        }
    }

    /**
     *
     * @param forceOpen
     * @return
     */
    public long getDegradeCount(boolean forceOpen) {
        if (forceOpen) {
            return forceOpenEntity.getDegradeCount();
        } else {
            return autoOpenEntity.getDegradeCount();
        }
    }

    /**
     *
     * @param forceOpen
     */
    public void addDegradeCount(boolean forceOpen) {
        if (forceOpen) {
            forceOpenEntity.addDegradeCount();
        } else {
            autoOpenEntity.addDegradeCount();
        }
    }

    private static class Entity {
        /**
         * 熔断请求统计
         */
        private AtomicLong degradeCount = new AtomicLong();

        /**
         * 熔断开启时间点
         */
        private AtomicLong circuitOpenedPoint = new AtomicLong();

        /**
         * 熔断恢复时间点
         */
        private AtomicLong circuitClosedPoint = new AtomicLong();


        public void markOpened() {
            circuitOpenedPoint.set(System.currentTimeMillis());
        }

        public void markClosed() {
            circuitClosedPoint.set(System.currentTimeMillis());
        }

        public long getCircuitDurationInSecond() {
            return (circuitClosedPoint.getAndSet(0) - circuitOpenedPoint.getAndSet(0)) / 1000;
        }

        public long getDegradeCount() {
            return degradeCount.getAndSet(0);
        }

        public void addDegradeCount() {
            degradeCount.getAndIncrement();
        }
    }
}
