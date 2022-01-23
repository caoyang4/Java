package src.designPattern.state.statemachine;

/**
 * @author caoyang
 */
public abstract class ThreadState {

    abstract void move(Action action);

    abstract void run();
}
