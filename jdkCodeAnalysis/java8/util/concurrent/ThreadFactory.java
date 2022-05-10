package java.util.concurrent;

/**
 * @code
 * class SimpleThreadFactory implements ThreadFactory {
 *   public Thread newThread(Runnable r) {
 *     return new Thread(r);
 *   }
 * }}
 */
// 用于生产线程
public interface ThreadFactory {

    Thread newThread(Runnable r);
}
