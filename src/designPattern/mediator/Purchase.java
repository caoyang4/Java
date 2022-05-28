package src.designPattern.mediator;

/**
 * 采购
 * @author caoyang
 * @create 2022-05-28 14:08
 */
public class Purchase extends AbstractRole{

    public Purchase(AbstractMediator mediator) {
        super(mediator);
    }
    // 采购
    public void buy(int number){
        super.mediator.execute("purchase.buy", number);
    }

    // 不再采购
    public void refuseBuy(){
        System.out.println("不再采购");
    }
}
