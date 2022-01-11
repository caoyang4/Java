package designMode.singleton;

/**
 * 静态内部类实现单例
 * @author caoyang
 */
public class SingleTonInnerClass {
    private SingleTonInnerClass() {}

    private static class SingletonInstance{
        private final static SingleTonInnerClass INSTANCE = new SingleTonInnerClass();
    }

    public static SingleTonInnerClass getInstance(){
        return SingletonInstance.INSTANCE;
    }

}
