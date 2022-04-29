package src.jvm;

import src.juc.JucUtils;

import java.util.ArrayList;
import java.util.List;

public class TestOOM {
    Object[] objects = new Object[1024];

    public static void main(String[] args) {
        List<TestOOM> list = new ArrayList<>();
        JucUtils.sleepSeconds(5);
        try {
            while (true){
                list.add(new TestOOM());
            }
        } catch (OutOfMemoryError e){
            System.out.println("OOM happens");
            throw e;
        }

    }
}
