package src.designPattern.observer;

import com.sun.istack.internal.NotNull;

public class DogObserver implements Observer{
    private void wang(@NotNull Child child){
        System.out.println(child.getName() + " is crying, dog wang wang wang...");
    }
    @Override
    public void wakeUpAction(@NotNull WakeUpEvent event) {
        wang(event.getChild());
    }
}
