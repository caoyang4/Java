package src.designPattern.state.statemachine;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author caoyang
 */

@AllArgsConstructor
@NoArgsConstructor
public class TerminatedStateThread extends ThreadState {
    private ThreadMachine machine;
    @Override
    public void move(Action action) {
        if("terminate".equals(action.getMsg())){
            System.out.println("terminate -> exit");
        }
    }

    @Override
    public void run() {

    }
}
