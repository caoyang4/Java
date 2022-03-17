package src.basis.fish;

import java.util.Calendar;

/**
 * synthetic 关键字
 * 如果同时用到了Enum和switch，如先定义一个enum枚举，然后用switch遍历这个枚举，
 * java编译器会偷偷生成一个synthetic的数组，数组内容是enum的实例。
 *
 * @author caoyang
 */
public class TestSynthetic {

    public static void main(final String[] arguments)
    {
        TestSynthetic.NestedClass nested = new TestSynthetic.NestedClass();
        // 此处能访问内部类的私有属性
        // 是因为编译器生成了一个package scope的access方法，是一个synthetic方法，作用相当于一个get方法
        // 在外部类使用highConfidential这个属性时，实际是使用了这个access方法，绕开了private成员变量的限制
        System.out.println("String: " + nested.highlyConfidential);
    }

    private static final class NestedClass
    {
        private String highlyConfidential = "too young, too simple";
        private int highlyConfidentialInt = 666;
        private Calendar highlyConfidentialCalendar = Calendar.getInstance();
        private boolean highlyConfidentialBoolean = true;
    }
}
