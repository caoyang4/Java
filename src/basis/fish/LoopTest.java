package src.basis.fish;

/**
 * @author caoyang
 */
public class LoopTest {
    public static void main(String[] args) {
        String young = "young";
        for (int i = 0; i < young.length(); ++i) {
            System.out.println(young.getBytes()[i]);
        }
    }
}
