package src.basis.fish;

import org.openjdk.jol.info.ClassLayout;

/**
 * @author caoyang
 */
public class TestCreateClass {
    private int i;
    private String  s;
    private double j;
    static {
        System.out.println("load block");
        n();
    }
    {
        System.out.println("init block");
        x();
    }
    public TestCreateClass(int i) {
        this.i = i;
        m();
    }
    private void m(){
        System.out.println("...m...");
    }
    private static void n(){ System.out.println("...n..."); }
    private void x(){ System.out.println("...x..."); }

    public static void main(String[] args) {
        TestCreateClass t = new TestCreateClass(100);
        System.out.println(ClassLayout.parseInstance(t).toPrintable());

    }
}
