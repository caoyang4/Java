package src.designPattern.factory.abstractFactory;

/**
 * 抽象动物类
 * @author caoyang
 */
public abstract class Animal implements PrintNameInterface{
    /**
     * 呼吸
     */
    abstract void breathe();

    /**
     * 进食
     */
    abstract void eat();

}
