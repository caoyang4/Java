package src.basis.reflection;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;

/**
 * 句柄访问
 * @author caoyang
 */
public class TestMethodHandle {
    public static void main(String[] args) throws Throwable {
        MethodHandles.Lookup lookup = Cobra.lookup();
        MethodType methodType = MethodType.methodType(void.class);
        // 操控静态方法
        MethodHandle methodHandle1 = lookup.findStatic(Cobra.class, "race", methodType);
        methodHandle1.invoke();
        // 操控成员方法、构造方法、父类方法
        MethodHandle methodHandle2 = lookup.findVirtual(Cobra.class, "say", methodType);
        methodHandle2.invoke(new Cobra());
        // 操控私有方法
        MethodHandle methodHandle3 = lookup.findSpecial(Cobra.class, "eat", methodType, Cobra.class);
        methodHandle3.invoke(new Cobra());
    }

}

class Cobra{
    public static void race(){
        System.out.println("race...");
    }
    public void say(){
        System.out.println("say...");
    }
    public static Lookup lookup(){
        return MethodHandles.lookup();
    }
    private void eat(){
        System.out.println("eat...");
    }
}
