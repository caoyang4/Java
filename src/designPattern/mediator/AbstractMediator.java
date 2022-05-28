package src.designPattern.mediator;

/**
 * @author caoyang
 * @create 2022-05-28 14:08
 */
public abstract class AbstractMediator {
    protected Purchase purchase;
    protected Sale sale;
    protected Storage storage;

    AbstractMediator() {
        purchase = new Purchase(this);
        sale = new Sale(this);
        storage = new Storage(this);
    }
    //中介者最重要的方法: 事件方法，处理多个对象之间的关系
    public abstract void execute(String str,Object...objects);
}
