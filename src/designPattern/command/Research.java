package src.designPattern.command;

/**
 * @author caoyang
 * @create 2022-05-26 13:45
 */
public class Research implements Invoker{
    private Command command;

    public Research(Command command) {
        this.command = command;
    }

    @Override
    public void execute() {
        System.out.println("RD do work:");
        command.executeCommand();
    }
}

 class QA implements Invoker{
    private Command command;

    public QA(Command command) {
        this.command = command;
    }

    @Override
    public void execute() {
        System.out.println("QA do test:");
        command.executeCommand();
    }
}


