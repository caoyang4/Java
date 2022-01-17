package src.designPattern.observer;

import com.sun.istack.internal.NotNull;

/**
 * @author caoyang
 */
public class DadObserver implements Observer{

    private void feed(@NotNull Child child){
        System.out.println(child.getName() + " is crying, dad feeds...");
    }

    @Override
    public void wakeUpAction(@NotNull WakeUpEvent event) {
        feed(event.getChild());
    }
}
