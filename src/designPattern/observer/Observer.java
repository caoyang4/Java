package src.designPattern.observer;

/**
 * 观察者接口
 * @author caoyang
 */
public interface Observer {
    void wakeUpAction(WakeUpEvent event);
}
