package src.designPattern.factory.abstractFactory;

/**
 * 儿童动物-猫咪
 * @author caoyang
 */
public class Cat extends Animal{
    @Override
    void breathe() {
        System.out.println("miao~");
    }

    @Override
    void eat() {
        System.out.println("xiu~");
    }

    @Override
    public void printName() {
        System.out.println("我是一只小猫咪...");
    }
}
