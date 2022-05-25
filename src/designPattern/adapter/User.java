package src.designPattern.adapter;

/**
 * 线程池的拒绝策略采用适配器模式
 * @author caoyang
 * @create 2022-05-25 15:26
 */
public class User {
    public static void main(String[] args) {
        Adapter adapter = new Adapter(() -> {
            System.out.println("交流电: 220v");
            return 220;
        });
        adapter.transfer();
    }
}
