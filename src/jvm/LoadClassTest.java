package src.jvm;

import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Random;

/**
 * -XX:+TraceClassLoading 追踪类加载信息
 * 类加载
 */
public class LoadClassTest {

    @Test
    public void loadObject() throws ClassNotFoundException {
        Class clz = Class.forName("java.lang.Object");
        Method[] methods = clz.getDeclaredMethods();
        StringBuilder builder = new StringBuilder();
        for (Method method : methods) {
            String modifier = Modifier.toString(method.getModifiers());
            builder.append(modifier).append(" ");
            String returnType = method.getReturnType().getSimpleName();
            builder.append(returnType).append(" ");
            String name = method.getName();
            builder.append(name).append("(");
            Class<?>[] parameters = method.getParameterTypes();
            for (int i = 0; i < parameters.length; i++) {
                String end = i == parameters.length-1 ? "" : ", ";
                builder.append(parameters[i].getSimpleName()).append(end);
            }
            builder.append(")\n");
        }
        System.out.println(builder);
    }

    @Test
    public void test1(){
        // 只会加载一次，即静态语句块只会执行一次
        System.out.println("first new B()");
        new B();
        System.out.println("second new B()");
        new B();
    }

    @Test
    public void test2(){
        // A
        // 233
        System.out.println(B.i);
    }

    @Test
    public void test3(){
        // java，直接从常量池获取，不需要加载类
        System.out.println(B.str);
    }
    @Test
    public void test4(){
        // 虽然 j 有 final 修饰，但是需要调用代码，所以需要执行 clinit 方法初始化，即需要类加载
        System.out.println(B.j);
    }
    @Test
    public void test5(){
        A t = new B();
        t.setNum();
        // a 的值为 1
        System.out.println("a = " + t.a);
    }

    static class A{
        static int a = 1;
        static int i = 233;
        static final String str = "java";
        static {
            System.out.println("clinit class A");
        }
        public void setNum(){
            System.out.println("A#setNum");
            a = 11;
        }
    }
    static class B extends A implements C{
        static int a = 2;
        static final int j = new Random().nextInt(10);
        static {
            System.out.println("clinit class B");
        }

        @Override
        public void setNum(){
            System.out.println("B#setNum");
            a = 22;
        }
    }

    interface C {
        Runnable r = new Runnable() {
             {
                 System.out.println("clinit interface C");
            }
            @Override
            public void run() {

            }
        };
        int k = new Random().nextInt(100);
        int m = 100;
    }

    @Test
    public void test6(){
        // load interface c， 说明接口初始化
        // 61
        System.out.println(B.k);
    }

    @Test
    public void test7(){
        // 100
        System.out.println(B.m);
    }

    @Test
    public void test8(){
        // load class A
        // load class B
        // 2
        // 父类会初始化，接口不会初始化
        System.out.println(B.a);
    }

}
