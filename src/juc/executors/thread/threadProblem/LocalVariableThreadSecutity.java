package src.juc.executors.thread.threadProblem;

/**
 * 局部变量线程安全问题
 * 内部产生，内部消亡则是线程安全
 * @author caoyang
 */
public class LocalVariableThreadSecutity {
    int number = 1;
    public void method0(){
        // 线程不安全
        number++;
    }
    public void method1(){
        // 线程安全
        StringBuilder builder = new StringBuilder();
        builder.append(1);
        builder.append(2);
    }

    public void method2(StringBuilder builder){
        // 线程不安全，可能多个线程操作builder
        builder.append("a");
        builder.append("b");
    }
    public StringBuilder method3(){
        // 线程不安全，线程对外暴露builder
        StringBuilder builder = new StringBuilder();
        builder.append(1);
        builder.append(2);
        return builder;
    }
    public String method4(){
        // 线程安全，String对象是线程安全对象
        StringBuilder builder = new StringBuilder();
        builder.append(1);
        builder.append(2);
        return builder.toString();
    }
    public void method5(){
        // 线程不安全
        StringBuilder builder = new StringBuilder();
        new Thread(() -> {
            builder.append("a");
            builder.append("b");
        }).start();
        method2(builder);
    }

}
