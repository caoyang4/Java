package src.basis.fish;

import org.junit.Test;

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

    @Test
    public void test1() {
        label:
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 3; j++) {
                if (j == 1) {
                    break label;
                }
                System.out.println("j=" + j);
            }
        }
    }

    @Test
    public void test2() {
        label:
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 3; j++) {
                if (j == 1) {
                    continue label;
                }
                System.out.println("j=" + j);
            }
        }

    }

}
