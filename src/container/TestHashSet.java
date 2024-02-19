package src.container;

import org.junit.Test;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class TestHashSet {

    @Test
    public void test1(){
        Set<String> set = new HashSet<>();
        set.add("james");
        set.add("kobe");
        Iterator<String> it = set.iterator();
        while (it.hasNext()){
            String tmp = it.next();
            if("kobe".equals(tmp)){
                // 会出现ConcurrentModificationException
                set.remove("kobe");
            }
            System.out.println(tmp);
        }
    }

    @Test
    public void test2(){
        Set<String> set = new HashSet<>();
        set.add("james");
        set.add("kobe");
        set.add(null);
        Iterator<String> it = set.iterator();
        while (it.hasNext()){
            String tmp = it.next();
            if("kobe".equals(tmp)){
                // 通过迭代器删除，不会出现ConcurrentModificationException
                it.remove();
            }
            System.out.println(tmp);
        }
        System.out.println(set.size());
    }

}
