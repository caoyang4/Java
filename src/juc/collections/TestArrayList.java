package src.juc.collections;

import java.util.*;

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

        new Thread(() -> {
            try {
                Iterator<String> iterator = list.iterator();
                while (iterator.hasNext()){
                    System.out.println("get " + iterator.next());
                    Thread.sleep(2000);
                }
            } catch (ConcurrentModificationException | InterruptedException e){
                System.out.println("ArrayList happens ConcurrentModificationException upon multi-thread");
            }
        }).start();

        int threadSize = 10;
        for (int i = 0; i < threadSize; i++) {
            Thread.sleep(1000);
            new Thread(() -> {
                System.out.println(Thread.currentThread().getName() + ": list add items");
                list.add(String.valueOf(new Random().nextInt(threadSize)));
            },"Thread"+(i+1)).start();
        }


    }

}
