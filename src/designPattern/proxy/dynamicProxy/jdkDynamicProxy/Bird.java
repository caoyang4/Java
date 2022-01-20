package src.designPattern.proxy.dynamicProxy.jdkDynamicProxy;

import src.designPattern.strategy.Tank;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 动态代理
 * @author caoyang
 */
public class Bird implements Flyable{
    @Override
    public void fly() {
        System.out.println("小鸟飞飞飞!!!");
    }

    public static void main(String[] args) {
        Bird bird = new Bird();
        System.setProperty("sun.misc.ProxyGenerator.saveGeneratedFiles", "true");
        Flyable flyable = (Flyable) Proxy.newProxyInstance(
                Bird.class.getClassLoader(),
                new Class[]{Flyable.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        System.out.println("jdkDynamicProxy start to" + method.getName());
                        Object o = method.invoke(bird, args);
                        System.out.println("jdkDynamicProxy end to" + method.getName());
                        return o;
                    }
                }
        );
        flyable.fly();

        System.out.println("=================");

        Flyable flyable1 = (Flyable) Proxy.newProxyInstance(
                Tank.class.getClassLoader(),
                new Class[] {Flyable.class},
                new LogHandler(bird)
        );

        flyable1.fly();

    }
}

class LogHandler implements InvocationHandler{
    private Bird bird;

    public LogHandler(Bird bird) {
        this.bird = bird;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("log proxy");
        Object o = method.invoke(bird, args);
        return o;
    }
}
