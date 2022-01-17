package src.designPattern.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * 观察者模式
 * @author caoyang
 */
public class Child {
    private String name;
    private boolean isCry = false;
    List<Observer> observers = new ArrayList<>();

    {
        observers.add(new DadObserver());
        observers.add(new MomObserver());
        observers.add(new DogObserver());
        observers.add(event -> {
            // 可以理解一个钩子函数
            System.out.println("hook");
        });
    }

    public Child(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isCry() {
        return isCry;
    }

    public void setCry(boolean cry) {
        isCry = cry;
    }

    /**
     * 事件源：child wake up
     */
    public void wakeup(){
        setCry(true);
        // 生成 event
        WakeUpEvent event = new WakeUpEvent(System.currentTimeMillis(), "floor", this);
        // 观察者接收到 event，随机产生响应 action
        observers.parallelStream().forEach(observer -> observer.wakeUpAction(event));
    }

    public static void main(String[] args) {
        Child child = new Child("xiaoMing");
        child.wakeup();
    }
}
