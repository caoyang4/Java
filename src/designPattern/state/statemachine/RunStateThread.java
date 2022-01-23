package src.designPattern.state.statemachine;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author caoyang
 */

@AllArgsConstructor
@NoArgsConstructor
public class RunStateThread extends ThreadState {
    private ThreadMachine machine;
    @Override
    public void move(Action action) {
        if("run".equals(action.getMsg())){
            System.out.println("run -> terminate");
            machine.setState(new TerminatedStateThread());
            run();
        }
    }

    @Override
    public void run() {
        machine.move(new Action("terminate"));
    }
}
