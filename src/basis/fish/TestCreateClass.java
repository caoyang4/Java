package src.basis.fish;

/**
 * @author caoyang
 */
public class TestCreateClass {
    private int i;
    static {
        System.out.println("load block");
    }
    {
        System.out.println("init block");
    }
    public TestCreateClass(int i) {
        this.i = i;
        m();
    }
    private void m(){
        System.out.println("...m...");
    }

    public static void main(String[] args) {
        TestCreateClass t = new TestCreateClass(100);
    }
}
