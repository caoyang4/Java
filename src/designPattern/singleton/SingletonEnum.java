package src.designPattern.singleton;

/**
 * 枚举实现单例
 * 无法被反射破坏  newInstance 不能实例化枚举类型
 * 无法被反序列化破坏  枚举 Enum 类重写 resolveObject 方法，抛异常
 * @author caoyang
 */

public class SingletonEnum {
    private SingletonEnum() {}

    private enum Singleton{
        /**
         * 枚举单例
         */
        INSTANCE;
        private final SingletonEnum instance;
        Singleton(){
            instance = new SingletonEnum();
        }
        private SingletonEnum getInstance(){
            return instance;
        }
    }

    public static SingletonEnum getInstance(){
        return Singleton.INSTANCE.getInstance();
    }

}
