package src.designPattern.factory.abstractFactory;

/**
 * 成人玩具-机器人
 * @author caoyang
 */
public class Robot extends Toy{
    @Override
    void rotate() {
        System.out.println("robot rotation");
    }

    @Override
    public void printName() {
        System.out.println("我是一个机器人...");
    }
}
