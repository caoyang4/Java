package src.basis.collections;

import org.junit.Test;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HashMapTest {
    public static void main(String[] args) {
        // Hashmap k,v 允许为 null
        System.out.println("hashmap");
        HashMap<String, String> map = new HashMap<>();
        map.put(null,null);

        // hashtable和concurrentHashmap 不允许 k，v 为 null
        System.out.println("hashtable");
        Hashtable<String, String> table = new Hashtable<>();
        table.put("null","null");
        System.out.println("concurrentHashmap");
        ConcurrentHashMap<String, String> concurrentHashMap = new ConcurrentHashMap<>();
        concurrentHashMap.put("null", "null");
    }

    @Test
    public void testAddAll(){
        List<String> list = new ArrayList<>();
        List<String> list1 = Arrays.asList("a","b","c");
        List<String> list2 = Arrays.asList("1","2","3");
        list.addAll(list1);
        System.out.println(list);
        list.addAll(list2);
        System.out.println(list);
    }
}
