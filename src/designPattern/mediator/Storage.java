package src.designPattern.mediator;

/**
 * 库存
 * @author caoyang
 * @create 2022-05-28 14:09
 */
public class Storage extends AbstractRole{
    public Storage(AbstractMediator mediator) {
        super(mediator);
    }
    // 初始库存数量
    private static int storageNumber =100;

    // 库存增加
    public void increase(int number){
        storageNumber += number;
        System.out.println("库存数量为:" + storageNumber);
    }
    // 库存降低
    public void decrease(int number){
        storageNumber -= number;
        System.out.println("库存数量为:" + storageNumber);
    }
    // 获得库存数量
    public int getStorageNumber(){
        return storageNumber;
    }
    // 存货积压，通知采购人员不要采购，销售人员要尽快销售
    public void clearStock(){
        System.out.println("清理存货数量为:"+storageNumber);
        super.mediator.execute("storage.clear");
    }
}
