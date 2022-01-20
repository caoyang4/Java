package src.designPattern.proxy.dynamicProxy.cglib;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * @author caoyang
 */
public class Animal {
    void eat(){
        System.out.println("大口吃肉");
    }

    void drink(){
        System.out.println("大口喝酒");
    }

    public static void main(String[] args) {
        Animal animal = new Animal();
        Enhancer enhancer = new Enhancer();
        enhancer.setClassLoader(Animal.class.getClassLoader());
        enhancer.setSuperclass(Animal.class);
        enhancer.setCallback((MethodInterceptor) (o, method, objects, methodProxy) -> {
            System.out.println("cglib start to " + method.getName());
            Object obj = method.invoke(animal, objects);
            System.out.println("cglib end to " + method.getName());
            return obj;
        });

        Animal animalCglib = (Animal) enhancer.create();
        animalCglib.eat();
        System.out.println("=============");
        animalCglib.drink();

    }
}
