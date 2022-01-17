package src.designPattern.strategy;

/**
 * @author caoyang
 */
public class FourDirFireStrategy implements FireStrategy{
    @Override
    public void fire(Tank tank) {
        System.out.println(tank.getName()+"四面八方开火！");
    }
}
