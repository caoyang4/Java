package src.designPattern.command;

import java.util.ArrayList;
import java.util.List;

/**
 * @author caoyang
 * @create 2022-05-26 13:48
 */
public class Leader {

    private List<Invoker> list = new ArrayList<>();
    public void order(Invoker invoker){
        list.add(invoker);
    }
    public void execute(){
        for (Invoker invoker : list) {
            invoker.execute();
        }
    }
    public static void main(String[] args) {
        Leader leader = new Leader();
        leader.order(new Research(new BackendCommand()));
        leader.order(new QA(new TestCommand()));
        leader.execute();
    }
}
