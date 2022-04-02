package src.juc.lock;

import lombok.extern.slf4j.Slf4j;
import org.openjdk.jol.info.ClassLayout;

/**
 * 调用 hashcode 偏向锁撤销
 * -XX:BiasedLockingStartupDelay=0 默认偏向锁立即生效
 * @author caoyang
 */
@Slf4j
public class TestBiasDismiss1 {
    public static void main(String[] args) {
        TestBiasDismiss1 test = new TestBiasDismiss1();
        log.debug(ClassLayout.parseInstance(test).toPrintable());
        test.hashCode();
        log.debug(ClassLayout.parseInstance(test).toPrintable());
    }
}
