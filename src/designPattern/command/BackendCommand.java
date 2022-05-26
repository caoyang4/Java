package src.designPattern.command;

/**
 * @author caoyang
 * @create 2022-05-26 13:47
 */
public class BackendCommand implements Command{

    @Override
    public void executeCommand() {
        System.out.println("C-R-U-D");
    }
}

class TestCommand implements Command{

    @Override
    public void executeCommand() {
        System.out.println("unit test");
    }
}
