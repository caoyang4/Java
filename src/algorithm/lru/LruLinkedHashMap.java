package src.algorithm.lru;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * LinkedHashMap 本身就已经具备了 LRU 的特性，
 * 只需要实现一点：当容器中元素个数超过我们设定的容量后，删除第一个元素即可。
 * 同时由于 LinkedHashMap 本身不具备线程安全，我们需要确保他线程安全
 * @author caoyang
 */
public class LruLinkedHashMap<K, V> extends LinkedHashMap<K, V> {

    /**
     * 容量
     */
    private int capacity;

    public LruLinkedHashMap(int capacity){
        super(capacity, 0.75f, true);
        this.capacity = capacity;
    }


    @Override
    public synchronized V put(K key, V value) {
        return super.put(key, value);
    }

    @Override
    public synchronized V get(Object key) {
        return super.get(key);
    }

    /**
     * 实现LRU的关键方法，如果 map 里面的元素个数大于了缓存最大容量，则删除链表的尾部元素
     * @param eldest
     * @return
     */
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity;
    }

    private static void printlnLru(LruLinkedHashMap lru){
        Iterator it = lru.keySet().iterator();
        while (it.hasNext()){
            System.out.print(it.next() + "\t");
        }
        System.out.println();
    }
    public static void main(String[] args) {
        LruLinkedHashMap lru = new LruLinkedHashMap(5);
        lru.put("1","a");
        lru.put("2","b");
        lru.put("3","c");
        lru.put("4","d");
        lru.put("5","e");
        System.out.println("插入 5 个元素");
        printlnLru(lru);
        System.out.println("插入 3 元素");
        lru.put("3","c");
        printlnLru(lru);
        System.out.println("插入第 6 个元素");
        lru.put("6","f");
        printlnLru(lru);
        System.out.println("访问 4 元素");
        lru.get("4");
        printlnLru(lru);
        System.out.println("插入第 7 个元素");
        lru.put("7","g");
        printlnLru(lru);
    }
}
