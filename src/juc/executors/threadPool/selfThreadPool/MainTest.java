package src.juc.executors.threadPool.selfThreadPool;

/**
 * @author caoyang
 */
public class MainTest {
    public static void main(String[] args) throws InterruptedException {
        MainTest mainTest = new MainTest();
        /**
         * 默认线程池
         */
        // 执行Main类里面的print函数
        SelfThreadFactory.getDefaultNormalPool().execute(mainTest, "testMethod");
        SelfThreadFactory.getDefaultNormalPool().execute(() -> System.out.println("this is DefaultNormalPool"));

        /**
         * 单线程池
         */
        SelfThreadFactory.getSinglePool().execute(mainTest, "testMethod");
        SelfThreadFactory.getSinglePool().execute(() -> System.out.println("this is SinglePool"));

        /**
         * 自定义线程池
         */
        ThreadPoolInterface selfPool1 = SelfThreadFactory.getSelfPool(2, 4, 1000);
        selfPool1.execute(mainTest, "testMethod");

        ThreadPoolInterface selfPool2 = SelfThreadFactory.getSelfPool(2, 3, 2000);
        selfPool2.execute(() -> System.out.println("this is SelfPool"));

        /**
         * 周期性线程池
         */
        SelfThreadFactory.getScheduledThreadPool().cycleExecute(() -> {
            System.out.println("this is ScheduledThreadPool");
        }, 0, 500, "cycleExecute");

        Thread.sleep(3000);
        System.out.println("关闭线程池");

        SelfThreadFactory.getScheduledThreadPool().shutDown();
        System.out.println("ScheduledThreadPool shut down");

        selfPool1.shutdown();
        System.out.println("selfPool1 shut down");
        selfPool2.shutdown();
        System.out.println("selfPool2 shut down");

        SelfThreadFactory.getSinglePool().shutdown();
        System.out.println("SinglePool shut down");

        SelfThreadFactory.getDefaultNormalPool().shutdown();
        System.out.println("DefaultNormalPool shut down");

    }

    public void testMethod(){
        System.out.println("...testMethod...");
    }
}
