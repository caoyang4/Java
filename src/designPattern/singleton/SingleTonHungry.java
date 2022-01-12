package src.designPattern.singleton;

/**
 * 饿汉式
 * @author caoyang
 */
public class SingleTonHungry {
    private static final SingleTonHungry INSTANCE = new SingleTonHungry();
    private SingleTonHungry() {}

    public static SingleTonHungry getInstance(){
        return INSTANCE;
    }
}
