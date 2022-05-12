package src.juc.collections;

import java.util.concurrent.ConcurrentSkipListMap;

/**
 * 基于跳表的hashmap
 */
public class TestConcurrentSkipListMap {
    public static void main(String[] args) {
        ConcurrentSkipListMap<Integer, String> map = new ConcurrentSkipListMap();
        map.put(1, "james");
        map.put(2, "paul");
        map.put(3, "kobe");
        map.put(4, "steve");
        System.out.println(map);
        System.out.println(map.keySet());
        System.out.println(map.descendingMap());
        System.out.println(map.descendingKeySet());
    }
}
