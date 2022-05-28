package src.designPattern.mediator;

/**
 * @author caoyang
 * @create 2022-05-28 16:14
 */
public class Mediator extends AbstractMediator{

    @Override
    public void execute(String str, Object... objects) {
        switch (str) {
            // 采购
            case "purchase.buy":
                this.buy((Integer) objects[0]);
                break;
            // 销售
            case "sale.sell":
                this.sell((Integer) objects[0]);
                break;
            // 折价销售
            case "sale.offsell":
                this.offSale();
                break;
            // 清仓处理
            case "stock.clear":
                this.clearStock();
                break;
        }
    }

    private void buy(int number) {
        int saleStatus = super.sale.getSaleStatus();
        if(saleStatus > 80){
            //销售情况良好
            System.out.println("销售情况良好，采购: " + number);
            super.storage.increase(number);
        }else{
            //销售情况不好,折半采购
            int buyNumber = number/2;
            super.storage.increase(buyNumber);
            System.out.println("销售情况不好，折半采购:" + buyNumber);
        }
    }
    private void sell(int number) {
        // 库存数量不够销售
        if(super.storage.getStorageNumber()<number){
            super.purchase.buy(number);
        }
        super.storage.decrease(number);
    }
    private void offSale() {
        System.out.println("折价销售 "+storage.getStorageNumber());
    }

    private void clearStock() {
        //要求清仓销售
        super.sale.offSale();
        //要求采购人员不要采购
        super.purchase.refuseBuy();
    }


}
