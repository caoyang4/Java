package src.basis.classLoad;

/**
 * 有且仅有6种情况必须立即对类进行初始化：
 * 1、遇到 new，getstatic，putstatic，invokestatic 指令
 * 2、通过 java.lang.reflect 反射调用
 * 3、当初始化类的时候，发现其父类没有初始化，先初始化父类
 * 4、虚拟机启动时候，main 方法主类初始化
 * 5、java.lang.invoke.MethodHandle
 * 6、含有 default 方法接口的实现类初始化时，先初始化该接口
 * @author caoyang
 */
public class TestClassLoader1 {
    public static void main(String[] args) {
        // 通过子类引用父类的静态变量，不会导致子类初始化
        // 对于静态变量，只有直接定义该字段的类才会被初始化
        System.out.println(SuperClass.value);

        // 没有触发SuperClass初始化，通过 newarray 指令触发 L...SuperClass 的初始化
        SuperClass[] classes = new SuperClass[10];

        // 常量在编译阶段置入常量池，故也不触发初始化SuperClass
        System.out.println(SuperClass.YOUNG);
    }
}

class SuperClass {
    static {
        System.out.println("SuperClass init!");
    }
    static int value = 123;
    final static String YOUNG = "young";
}

class SubClass extends SuperClass {
    static {
        System.out.println("SubClass init!");
    }
}