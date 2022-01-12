package src.designPattern.factory;

/**
 * 简单工厂模式
 * 扩展性不足
 * @author caoyang
 */
public class VehicleFactory {
    public Car createCar(){
        return new Car();
    }

    public Plane createPlane(){
        return new Plane();
    }
}
