package src.jvm;

import src.juc.JucUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * 内存泄漏
 * 缓存泄漏
 * @author caoyang
 * @create 2022-05-31 23:27
 */
public class MemoryLeak3 {
    public static Map<String, String> map = new HashMap<>();
    public static Map<String, String> wMap = new WeakHashMap<>();

    public static void init(){
        String ref1 = new String("object1");
        String ref2 = new String("object2");
        map.put(ref1, "cache1");
        map.put(ref2, "cache2");
        String ref3 = new String("object3");
        String ref4 = new String("object4");
        wMap.put(ref3, "cache3");
        wMap.put(ref4, "cache3");
        System.out.println("ref1, ref2, ref3, ref4引用消失");
    }
    public static void printWeakHashMap(){
        System.out.println("WeakHashMap before gc");
        for (Map.Entry<String, String> o : wMap.entrySet()) {
            System.out.println(o);
        }
        System.gc();
        JucUtils.sleepSeconds(1);
        System.out.println("WeakHashMap after gc");
        for (Map.Entry<String, String> o : wMap.entrySet()) {
            System.out.println(o);
        }
    }
    public static void printHashMap(){
        System.out.println("HashMap before gc");
        for (Map.Entry<String, String> o : map.entrySet()) {
            System.out.println(o);
        }
        System.gc();
        JucUtils.sleepSeconds(1);
        System.out.println("WeakHashMap after gc");
        for (Map.Entry<String, String> o : map.entrySet()) {
            System.out.println(o);
        }
    }
    public static void main(String[] args) {
        init();
        System.out.println("WeakHashMap");
        // WeakHashMap持有的 ref3， ref4 被回收
        printWeakHashMap();
        System.out.println();
        // HashMap持有的 ref1， ref2 未被回收，导致内存泄漏
        System.out.println("HashMap");
        printHashMap();
    }
}
