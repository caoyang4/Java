package src.juc.executors.thread.threadProblem;

import src.juc.JucUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 局部变量线程不安全示例
 * @author caoyang
 */
public class TestLocalVariableThread {
    public static void main(String[] args) {
        try {
            while (true){
                SubTestLocalVariableThread test = new SubTestLocalVariableThread();
                for (int i = 0; i < 10; i++) {
                    new Thread(() -> {
                        test.action(100);
                    }, "thread" + i).start();
                }
            }
        } catch (Exception e){
            throw e;
        }

    }
    public void action(int number){
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            add(list);
            remove(list);
        }

    }
    public void add(List<Integer> list){
        list.add(1);
        JucUtils.sleepSeconds(1);
    }
    public void remove(List<Integer> list){
        list.remove(0);
    }

}

class SubTestLocalVariableThread extends TestLocalVariableThread{
    /**
     * 子类重写父类方法，开启线程，可能导致线程不安全
     * @param list
     */
    @Override
    public void remove(List<Integer> list){
        new Thread(() -> {
            list.remove(0);
        }).start();
    }
}
