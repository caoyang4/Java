package src.juc.collections;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * ArrayList 线程不安全
 * @author caoyang
 */
public class TestArrayList {
    public static void main(String[] args) throws InterruptedException {
        List<String> list = new ArrayList<>();
//        List<String> list = new CopyOnWriteArrayList<>();
        list.add("shanghai");
        list.add("beijing");
        Iterator<String> iterator = list.iterator();

        int threadSize = 10;
        for (int i = 0; i < threadSize; i++) {
            new Thread(() -> {
                    System.out.println(Thread.currentThread().getName() + ": list add items");
                    list.add(String.valueOf(new Random().nextInt(threadSize)));
            },"Thread"+(i+1)).start();
        }

        new Thread(() -> {
            try {
                while (iterator.hasNext()){
                    System.out.println("get " + iterator.next());
                    Thread.sleep(1000);
                }
            } catch (ConcurrentModificationException | InterruptedException e){
                System.out.println("ArrayList happens ConcurrentModificationException upon multi-thread");
            }
        }).start();


    }

}
