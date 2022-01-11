package src.basis.lambda;

/**
 * 1、接口有且只能有个一个抽象方法，只有方法定义，没有方法体
 * 2、在接口中覆写Object类中的public方法，不算是函数式接口的方法。
 * FunctionalInterface注解可有可无
 * @author caoyang
 */


@FunctionalInterface
public interface FunctionInterfaceTest {
    String getInfo(String input);

    @Override
    String toString();  //Object中的方法

    @Override
    boolean equals(Object obj); //Object中的方法

    default void m(){
        System.out.println("interface default 方法");
    }
}
