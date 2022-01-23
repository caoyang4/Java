package src.designPattern.state.statemachine;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author caoyang
 */

@AllArgsConstructor
@NoArgsConstructor
public class NewStateThread extends ThreadState {
    private ThreadMachine machine;
    @Override
    public void move(Action action) {
        if("start".equals(action.getMsg())){
            System.out.println("start -> run");
            machine.setState(new RunStateThread(machine));
            run();
        }
    }

    @Override
    public void run() {
        machine.move(new Action("run"));
    }
}
