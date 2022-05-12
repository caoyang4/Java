package src.jvm;

/**
 * 对象是否被回收
 * finalize方法是对象逃脱 gc 的最后机会
 * @author caoyang
 */
public class FinalizeEscapeGC {
    public static FinalizeEscapeGC hook = null;
    public void isAlive(){
        System.out.println("yes, I am alive...");
    }
    public static void isDead(){
        System.out.println("no, I am dead...");
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        System.out.println("finalize method executed");
        hook = this;
    }

    public static void main(String[] args) throws InterruptedException {
        hook = new FinalizeEscapeGC();
        /*
        调用 gc 时，若对象的finalize未被调用过，会被调用，因此第一次gc不会被回收
         */
        hook = null;
        // full gc
        System.gc();
        System.out.println("first gc");
        // Finalizer线程优先级很低，等待让它执行
        Thread.sleep(1000);
        if (hook != null){
            hook.isAlive();
        } else {
            isDead();
        }

        // finalize方法只会被系统调用一次，第二次无法逃脱gc，被回收
        hook = null;
        System.gc();
        System.out.println("second gc");
        Thread.sleep(1000);
        if (hook != null){
            hook.isAlive();
        } else {
            isDead();
        }

    }
}
