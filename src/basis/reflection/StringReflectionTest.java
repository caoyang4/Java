package src.basis.reflection;

import java.lang.reflect.Field;

/**
 * 反射机制破坏破坏不可变性
 * @author caoyang
 */
public class StringReflectionTest {
    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        String str0 = "young";
        String name = "young";
        // name 和 str0 指向同一个内存地址  常量池
        System.out.println("str0: " + str0);
        System.out.println("name: " + name);

        // 获取字符数组： char[] value
        Field field = String.class.getDeclaredField("value");
        // 改变权限
        field.setAccessible(true);
        char[] value = (char[]) field.get(name);
        // 通过改变 char[] 的元素，破坏 String 的不可变性
        value[0] = 'Y';
        System.out.println("after reflection");
        // name的地址指向的内容被改变，str0由于地址一样，内容也会随name变化
        System.out.println("str0：" + str0);
        System.out.println("name：" + name);

        String str1 = "Young";
        String str2 = "young";

        System.out.println("========str1 compare name===========");
        // str1 与 name 的内容都是 Young，地址不一样
        System.out.println("==: " + (str1 == name));
        System.out.println("equals: " + (str1.equals(name)));

        System.out.println("========str2 compare name===========");
        /*
         jvm加载时，发现 str2 内容也是 young，且常量池已有 young，则直接
         将 young 的内存地址赋值给str2，即 name， str0， str2 的内存地址相同
         */
        // true 地址一样
        System.out.println("==: " + (str2 == name));
        // true 内容也一样: Young
        System.out.println("equals: " + (str2.equals(name)));

        System.out.println("==============");
        System.out.println("name: " + name);
        System.out.println("str0: " + str0);
        System.out.println("str1: " + str1);
        System.out.println("str2: " + str2);

    }
}
