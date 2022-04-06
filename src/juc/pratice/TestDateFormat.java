package src.juc.pratice;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * @author caoyang
 */
public class TestDateFormat {
    public static void main(String[] args) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                synchronized (sdf) {
                    try {
                        // SimpleDateFormat非线程安全，需要加锁保证线程安全
                        System.out.println(sdf.parse("1993-11-01"));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
}
