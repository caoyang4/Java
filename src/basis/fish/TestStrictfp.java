package src.basis.fish;

/**
 * strictfp 关键字可应用于类、接口或方法
 * 是strict float point的缩写，指的是精确浮点，它是用来确保浮点数运算的准确性。
 * @author caoyang
 */
public class TestStrictfp {
    strictfp static void demo(){
        float aFloat = 0.6710339f;
        double aDouble = 0.04150553411984792d;
        double sum = aFloat + aDouble;
        float quotient = (float)(aFloat / aDouble);
        System.out.println("float: " + aFloat);
        System.out.println("double: " + aDouble);
        System.out.println("sum: " + sum);
        System.out.println("quotient: " + quotient);
    }

    public static void main(String[] args) {
        demo();
    }
}
