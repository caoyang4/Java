package src.jvm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.ref.SoftReference;

/**
 * 软引用
 * oom 之前，会回收
 * -Xms10m -Xmx10m -XX:+PrintGCDetails
 */
@Slf4j(topic = "SoftReferenceTest")
public class SoftReferenceTest {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class User{
        String name;
        int id;
    }

    public static void main(String[] args) {
        /**
         * 等价于
         * User user = new User("young", 66);
         * SoftReference<User> softReference = new SoftReference<>(user);
         * user = null;
         *
         */
        SoftReference<User> softReference = new SoftReference<>(new User("young", 66));
        System.gc();
        System.runFinalization();
        // 第一次 gc，空间够，不会回收
        log.info("first gc: user {}", softReference.get());
        try {
            byte[] bytes = new byte[1024 * 1024 * 16];
        } catch (OutOfMemoryError e){
            throw e;
        } finally {
            // 在报OOM之前，会发生 GC，回收软引用，如果资源还不够，就会报 OOM
            log.info("second gc: user {}", softReference.get());
        }


    }
}
