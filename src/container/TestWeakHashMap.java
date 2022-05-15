package src.container;

import java.util.Map;
import java.util.WeakHashMap;

public class TestWeakHashMap {
    public static void main(String[] args) {
        Map<String, Integer> map = new WeakHashMap<>();
        // 放入3个new String()声明的字符串
        map.put(new String("1"), 1);
        map.put(new String("2"), 2);
        map.put(new String("3"), 3);

        // 放入常量字符串
        map.put("4", 4);

        String test = null;
        for (String s : map.keySet()) {
            if("3".equals(s)){
                // 使用key强引用new String("3")这个key
                test = s;
            }
        }
        // {4=4, 1=1, 2=2, 3=3}
        System.out.println("before first gc: " + map);
        System.gc();
        System.runFinalization();
        //  {4=4, 5=5, 3=3}，key为new String("3")的Entry 未被回收，因为有 test 强引用存在
        System.out.println("after first gc: " + map);
        map.put(new String("5"), 5);
        System.out.println("before second gc: " + map);
        // key与new String("3")断裂
        test = null;
        System.gc();
        System.out.println("after second gc: " + map);

    }
}
