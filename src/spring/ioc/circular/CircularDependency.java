package src.spring.ioc.circular;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import src.spring.ioc.MyFileSystemXmlApplicationContext;

/**
 * IoC 循环依赖
 * @author caoyang
 */
public class CircularDependency {
    public static void main(String[] args) {
//        ApplicationContext ctx = new FileSystemXmlApplicationContext("src/spring/spring.xml");
        ApplicationContext ctx = new MyFileSystemXmlApplicationContext("src/spring/spring.xml");
        /*
         循环依赖示例
         */
        TestA testA = (TestA) ctx.getBean("testA");
        TestB testB = (TestB) ctx.getBean("testB");
        TestC testC = (TestC) ctx.getBean("testC");
        System.out.println("TestA: " + testA + "\n\ttestB: " + testA.getTestB());
        System.out.println("TestB: " + testB + "\n\ttestC: " + testB.getTestC());
        System.out.println("TestC: " + testC + "\n\ttestC: " + testC.getTestA());

        System.out.println();

        X x = (X) ctx.getBean("x");
        Y y = (Y) ctx.getBean("y");
        Z z = (Z) ctx.getBean("z");
        System.out.println("X: " + x);
        System.out.println("\tname: " + x.getName());
        System.out.println("\ty: " + x.getY());
        System.out.println("\t\tyName: " + x.getY().getName());
        System.out.println("\tz: " + x.getZ());
        System.out.println("\t\tzName: " + x.getZ().getName());
        System.out.println("Y: " + y);
        System.out.println("\tname: " + y.getName());
        System.out.println("\tx: " + y.getX());
        System.out.println("\t\txName: " + y.getX().getName());
        System.out.println("Z: " + z);
        System.out.println("\tname: " + z.getName());
        System.out.println("\tx: " + z.getX());
        System.out.println("\t\txName: " + z.getX().getName());

    }
}
