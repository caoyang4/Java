package src.designPattern.mediator;

/**
 * 角色——抽象类
 * @author caoyang
 * @create 2022-05-28 16:17
 */
public abstract class AbstractRole {
    protected AbstractMediator mediator;

    public AbstractRole(AbstractMediator mediator) {
        this.mediator = mediator;
    }
}
