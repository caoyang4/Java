package src.jvm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import src.juc.JucUtils;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;

/**
 * 虚引用
 */
@Slf4j(topic = "PhantomReferenceTest")
public class PhantomReferenceTest {
    static PhantomReferenceTest test;
    static ReferenceQueue<PhantomReferenceTest> userReferenceQueue;

    public static void main(String[] args) {
        test = new PhantomReferenceTest();
        // 指定引用队列
        userReferenceQueue = new ReferenceQueue<>();
        Thread thread = new CheckThread();
        thread.setDaemon(true);
        thread.start();
        PhantomReference<PhantomReferenceTest> phantomReference = new PhantomReference<>(test, userReferenceQueue);
        test = null;
        log.info("test in PhantomReference: {}", phantomReference.get());
        System.gc();
        JucUtils.sleepSeconds(1);
        test = null;
        System.gc();
    }

    @Override
    protected void finalize() throws Throwable {
        test = this;
        log.info("call finalize, test 幸存：{}", test);
    }

    static class CheckThread extends Thread{
        @Override
        public void run() {
            while (true){
                if (userReferenceQueue != null){
                    PhantomReference<PhantomReferenceTest> phantomReference = null;
                    try {
                        phantomReference = (PhantomReference<PhantomReferenceTest>) userReferenceQueue.remove();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (phantomReference != null) {
                        log.info("追踪 gc 过程，PhantomReferenceTest对象被回收了");
                    }
                }
            }
        }
    }
}
