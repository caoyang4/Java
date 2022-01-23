package src.designPattern.state.statemachine;

import lombok.Data;

/**
 * @author caoyang
 */
@Data
public class ThreadMachine {
    private ThreadState state;

    void move(Action action){
        state.move(action);
    }
    void run(){
        state.run();
    }

    public static void main(String[] args) {
        ThreadMachine machine = new ThreadMachine();
        machine.setState(new NewStateThread(machine));
        machine.move(new Action("start"));
    }
}
