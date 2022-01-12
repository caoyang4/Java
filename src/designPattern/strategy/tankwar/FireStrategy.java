package src.designPattern.strategy.tankwar;

/**
 * 开火策略接口
 * @author caoyang
 */
public interface FireStrategy {
    /**
     * 开火
     * @param tank 坦克
     */
    void fire(Tank tank);
}
