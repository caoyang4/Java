package src.container;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public class TestIdentityHashMap {
    public static void main(String[] args) {
        /*
        当HashMap对4个key执行put操作时，使用hashCode作为hash值，使用equals进行相等判断，
            后3个put操作，最终执行的是更新操作，最后HashMap中只有1项。
        而IdentityHashMap对4个key执行put操作时，使用System.identityHashCode作为hash值，使用==进行相等判断，
        后3个put操作，最终执行的是插入操作，最后IdentityHashMap中有4项
         */
        Map<Object, Object> hashMap = new HashMap<>();
        System.out.println("HashMap");
        put(hashMap, "1", "1");
        put(hashMap, String.valueOf(1), "2");
        put(hashMap, String.valueOf('1'), "3");
        put(hashMap, new String("1"), "4");
        System.out.println();

        System.out.println("IdentityHashMap");
        Map<Object, Object> identityHashMap = new IdentityHashMap<>();
        put(identityHashMap, "1", "1");
        put(identityHashMap, String.valueOf(1), "2");
        put(identityHashMap, String.valueOf('1'), "3");
        put(identityHashMap, new String("1"), "4");
    }


    public static void put(Map<Object, Object> map, Object key, Object value) {
        map.put(key, value);
        System.out.println(map);
    }
}