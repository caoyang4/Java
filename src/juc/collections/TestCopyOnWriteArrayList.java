package src.juc.collections;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * @author caoyang
 */
public class TestCopyOnWriteArrayList {
    public static void main(String[] args) throws InterruptedException {
        CopyOnWriteArrayList<Integer> list = new CopyOnWriteArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        // list弱一致性，后续其他线程的增删改不影响iter的遍历
        Iterator<Integer> iter = list.iterator();
        new Thread(() -> {
            list.remove(0);
            System.out.println(list);
        }).start();
        TimeUnit.SECONDS.sleep(1);
        while (iter.hasNext()) {
            System.out.println(iter.next());
        }
    }
}
