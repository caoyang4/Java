package src.container;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * ConcurrentModificationException
 */
public class TestList2 {
    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        Iterator iterator = list.iterator();
        list.add(3); // 会报ConcurrentModificationException
        while (iterator.hasNext()){
            System.out.println(iterator.next());
        }
    }
}
