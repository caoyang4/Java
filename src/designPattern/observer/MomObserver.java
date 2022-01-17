package src.designPattern.observer;

import com.sun.istack.internal.NotNull;

public class MomObserver implements Observer{
    private void hug(@NotNull Child child){
        System.out.println(child.getName() +" is crying, mom hugs");
    }

    @Override
    public void wakeUpAction(@NotNull WakeUpEvent event) {
        hug(event.getChild());
    }
}
