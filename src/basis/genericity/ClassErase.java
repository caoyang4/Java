package src.basis.genericity;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

/**
 * 泛型类型擦除验证
 * @author caoyang
 */
public class ClassErase {
    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        ArrayList<String> list1 = new ArrayList<String>();
        list1.add("abc");

        ArrayList<Integer> list2 = new ArrayList<Integer>();
        list2.add(123);

        // true
        System.out.println(list1.getClass() == list2.getClass());


        ArrayList<Integer> list = new ArrayList<Integer>();

        //这样调用 add 方法只能存储整形，因为泛型类型的实例为 Integer
        list.add(1);

        // 利用反射调用add()方法的时候，可以存储字符串
        // 说明Integer泛型实例在编译之后被擦除掉了，只保留了原始类型，即 Object 类型
        list.getClass().getMethod("add", Object.class).invoke(list, "asd");

        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i));
        }
    }
}
