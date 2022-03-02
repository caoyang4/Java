package src.juc.collections;

import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author caoyang
 */
public class TestCopyOnWriteArrayList {
    public static void main(String[] args) throws InterruptedException {
        CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();
        for (int i = 0; i < 5; i++) {
            list.add(UUID.randomUUID().toString().substring(0,8));
        }
        Iterator<String> iterator = list.iterator();
        TestCopyOnWriteArrayListThread thread = new TestCopyOnWriteArrayListThread("ThreadCOW", list);
        thread.start();
        while (iterator.hasNext()){
            System.out.print(iterator.next() + "\t");
        }
        System.out.println();
        Thread.sleep(2000);
        iterator = list.iterator();
        while (iterator.hasNext()){
            System.out.print(iterator.next() + "\t");
        }

    }
}

class TestCopyOnWriteArrayListThread extends Thread{
    CopyOnWriteArrayList list;

    public TestCopyOnWriteArrayListThread(String name, CopyOnWriteArrayList list) {
        super(name);
        this.list = list;
    }

    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {
            list.add(UUID.randomUUID().toString().substring(0,8));
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}