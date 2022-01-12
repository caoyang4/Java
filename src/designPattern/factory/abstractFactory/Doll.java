package src.designPattern.factory.abstractFactory;

/**
 * 儿童玩具-布娃娃
 * @author caoyang
 */
public class Doll extends Toy{
    @Override
    void rotate() {
        System.out.println("doll rotation");
    }

    @Override
    public void printName() {
        System.out.println("我是一个布娃娃...");
    }
}
