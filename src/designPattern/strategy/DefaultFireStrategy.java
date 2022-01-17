package src.designPattern.strategy;

/**
 * 默认开火策略
 * 单例模式
 *
 * 不同的开火策略，子弹类型
 * 新建特定类实现 FireStrategy 接口即可
 * 满足开闭原则
 * @author caoyang
 */
public class DefaultFireStrategy implements FireStrategy{
    private static final DefaultFireStrategy INSTANCE = new DefaultFireStrategy();

    private DefaultFireStrategy(){}

    public static DefaultFireStrategy getInstance(){
        return INSTANCE;
    }

    @Override
    public void fire(Tank tank) {
        System.out.println(tank.getName()+"默认开火！");
    }
}
