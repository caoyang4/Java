package src.basis.fish;

/**
 * Integer 测试
 * @author caoyang
 */
public class IntegerTest {
    public static void main(String[] args) {
        doubleDengyu();
    }
    public static void doubleDengyu(){
        Integer a = 127;
        Integer b = 127;
        Integer c = new Integer(127);
        int d =127;
        System.out.println("127 a==b: " + (a==b));
        System.out.println("127 b==c: " + (b==c));
        System.out.println("127 b==d: " + (b==d));
        System.out.println("127 c==d: " + (c==d));
        System.out.println("127 a==d: " + (a==d));

        Integer e = 128;
        Integer f = 128;
        System.out.println("128 e==f: " + (e==f));
    }
}
