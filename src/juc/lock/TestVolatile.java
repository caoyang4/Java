package src.juc.lock;

/**
 * volatile 保证可见性和有序性
 *
 * 共享的long和double变量的为什么要用volatile?
 * 因为long和double两种数据类型的操作可分为高32位和低32位两部分，因此普通的long或double类型读/写可能不是原子的
 * 而volatile变量的单次读/写操作可以保证原子性的，不能保证 i++ 的原子性
 *
 * volatile 变量的内存可见性是基于内存屏障(Memory Barrier)实现
 *
 * @author caoyang
 */
public class TestVolatile {
    volatile int i = 0;
    public void increase(){
        i++;
    }

    public int getI() {
        return i;
    }

    public static void main(String[] args) {
        TestVolatile testVolatile = new TestVolatile();
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    testVolatile.increase();
                }
            }).start();
        }
        /*
         每次运行结果都不一致，都是一个小于 10000 的数字
         volatile是无法保证原子性的(否则结果应该是1000)。原因也很简单，i++其实是一个复合操作，
         包括三步骤：
            读取i的值;
            对i加1;
            将i的值写回内存
        * */

        System.out.println("最终结果:" + testVolatile.getI());
    }
}
