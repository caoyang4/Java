package src.basis.fish;

/**
 * @author caoyang
 */
public class StringTest {
    public static void main(String[] args) {
        String a = new String("xyz");
        String b = new String("xyz");
        // false
        System.out.println(a == b);

        String c1 = "xyz";
        String c2 = "xyz";
        String d = new String("xyz").intern();

        // false
        System.out.println(a == c1);
        // true
        System.out.println(c1 == c2);
        // true
        System.out.println(c1 == d);
    }
}
