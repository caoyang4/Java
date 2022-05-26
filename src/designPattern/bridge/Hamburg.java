package src.designPattern.bridge;

/**
 * @author caoyang
 * @create 2022-05-26 12:29
 */
public class Hamburg implements Product{
    @Override
    public void produce() {
        System.out.println("Hamburg is produced");
    }

    @Override
    public void sell() {
        System.out.println("Hamburg for sale");
    }
}
