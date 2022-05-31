package src.jvm;

import java.util.ArrayList;
import java.util.List;

/**
 * 内存泄漏
 * 单例与静态集合类
 * @author caoyang
 * @create 2022-05-31 22:49
 */
public class MemoryLeak1 {
    static List<Object> lists = new ArrayList<>();
    Object object;
    final static MemoryLeak1 SINGLE_TON = new MemoryLeak1();

    public static MemoryLeak1 getInstance(){
        Object obj = new Object();
        // 单例持有 obj 引用，也无法被回收
        SINGLE_TON.object = obj;
        return SINGLE_TON;
    }
    public void leak(){
        // 局部变量，长周期静态变量lists持有，导致不能被回收
        Object obj = new Object();
        lists.add(obj);
    }
}
