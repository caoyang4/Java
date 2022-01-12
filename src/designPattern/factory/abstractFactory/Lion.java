package src.designPattern.factory.abstractFactory;

/**
 * 成人动物-狮子
 * @author caoyang
 */
public class Lion extends Animal{
    @Override
    void breathe() {
        System.out.println("hou~");
    }

    @Override
    void eat() {
        System.out.println("wua...");
    }

    @Override
    public void printName() {
        System.out.println("我是一头猛狮子...");
    }
}
