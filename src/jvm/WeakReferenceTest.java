package src.jvm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.ref.WeakReference;

/**
 * 弱引用
 * 只要 gc，就会回收
 */
@Slf4j(topic = "WeakReferenceTest")
public class WeakReferenceTest {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class User{
        String name;
        int id;
    }

    public static void main(String[] args) {
        WeakReference<User>  weakReference = new WeakReference<>(new User("james", 60));
        log.info("before gc, user: {}", weakReference.get());
        System.gc();
        // 增加 gc 发生几率
        System.runFinalization();
        log.info("after gc, user: {}", weakReference.get());
    }
}
