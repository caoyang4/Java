package src.designPattern.mediator;

import java.util.Random;

/**
 * 销售
 * @author caoyang
 * @create 2022-05-28 14:08
 */
public  class Sale extends AbstractRole{

    public Sale(AbstractMediator mediator) {
        super(mediator);
    }

    //销售IBM型号的电脑
    public void sell(int number){
        super.mediator.execute("sale.sell", number);
        System.out.println("销售数量: " + number);
    }
    // 反馈销售情况
    public int getSaleStatus(){
        Random rand = new Random(System.currentTimeMillis());
        int saleStatus = rand.nextInt(100);
        System.out.println("销售情况为: "+saleStatus);
        return saleStatus;
    }
    //折价处理
    public void offSale(){
        super.mediator.execute("sale.offsell");
    }
}
