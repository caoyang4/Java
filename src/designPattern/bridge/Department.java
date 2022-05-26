package src.designPattern.bridge;

/**
 * @author caoyang
 * @create 2022-05-26 12:30
 */
public class Department implements Product{
    @Override
    public void produce() {
        System.out.println("Department is built");
    }

    @Override
    public void sell() {
        System.out.println("Department for sale");
    }
}
