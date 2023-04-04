package src.kilim;

import kilim.Mailbox;
import kilim.Task;
import kilim.Pausable;

import java.util.ArrayList;
import java.util.List;

import static java.lang.System.*;

/**
 * @author caoyang
 * @create 2023-04-04 11:55
 */

public class KilimTest {

    public static void main(String[] args) throws Pausable {
        new KilimTest().execute();
    }

    public void execute() throws Pausable {
        System.out.println("Hello ");
        Task.sleep(100);
        System.out.println("World!");
    }

}