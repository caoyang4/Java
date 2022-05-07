package src.basis.fish;

public class IntegerTest2 {
    public static void main(String[] args) {
        Integer a = 1;
        Integer b = 2;
        Integer c = 3;
        Integer d = 3;
        Integer e = 321;
        Integer f = 321;
        Long g = 3L;
        // true
        System.out.println("c==d: " + (c==d));
        // false
        System.out.println("e==f: " + (e==f));
        // true
        System.out.println(c == (a+b));
        // true 类型和数值一样
        System.out.println(c.equals(a+b));
        // true 比较数值
        System.out.println(g == (a+b));
        // false 类型不一样
        System.out.println(g.equals(a+b));
    }
}
