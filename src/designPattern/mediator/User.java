package src.designPattern.mediator;

/**
 * 中介者模式也叫做调停者模式：中介者模式适用于多个对象之间紧密耦合，在类图中出现了蜘蛛网状结构，在这种情况下可以考虑使用中介者模式，
 * 有利于把蜘蛛网梳理为一个星型结构，使原本复杂混乱关系变得清晰简单
 * 采购-销售-库存模型
 * 采用星型拓扑结构
 * @author caoyang
 * @create 2022-05-28 14:07
 */
public class User {
    public static void main(String[] args) {
        AbstractMediator mediator = new Mediator();
        //采购人员采购电脑
        System.out.println("------采购--------");
        Purchase purchase = new Purchase(mediator);
        purchase.buy(100);
        //销售人员销售电脑
        System.out.println("------销售--------");
        Sale sale = new Sale(mediator);
        sale.sell(10);
        // 库存
        System.out.println("------清理库存--------");
        Storage storage = new Storage(mediator);
        storage.clearStock();
    }
}
