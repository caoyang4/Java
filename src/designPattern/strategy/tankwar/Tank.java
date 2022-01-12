package src.designPattern.strategy.tankwar;

/**
 * 策略模式
 * 坦克大战
 * @author caoyang
 */
public class Tank {
    private String name;
    FireStrategy fireStrategy;
    public Tank() {}

    public Tank(TankEnum tankEnum){
        fireStrategy = tankEnum == TankEnum.ENEMY ? DefaultFireStrategy.getInstance() : new FourDirFireStrategy();
    }
    public String getName() {
        return name;
    }

    public FireStrategy getFireStrategy() {
        return fireStrategy;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFireStrategy(FireStrategy fireStrategy) {
        this.fireStrategy = fireStrategy;
    }

    void fire(FireStrategy strategy){
        strategy.fire(this);
    }

    @Override
    public String toString() {
        return name;
    }


}
