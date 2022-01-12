package src.designPattern.factory;

/**
 * 汽车工厂
 * 定制汽车生产过程
 * @author caoyang
 */
public class CarFactory {
    public Moveable create(){
        return new Car();
    }
}
