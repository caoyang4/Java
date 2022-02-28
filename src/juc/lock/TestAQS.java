package src.juc.lock;

/**
 * AbstractQueuedSynchronizer
 * AQS使用了模板方法模式，自定义同步器时需要重写下面几个AQS提供的模板方法：
 * isHeldExclusively()//该线程是否正在独占资源。只有用到condition才需要去实现它。
 * tryAcquire(int)//独占方式。尝试获取资源，成功则返回true，失败则返回false。
 * tryRelease(int)//独占方式。尝试释放资源，成功则返回true，失败则返回false。
 * tryAcquireShared(int)//共享方式。尝试获取资源。负数表示失败；0表示成功，但没有剩余可用资源；正数表示成功，且有剩余资源。
 * tryReleaseShared(int)//共享方式。尝试释放资源，成功则返回true，失败则返回false。
 *
 * @author caoyang
 */
public class TestAQS {
}
