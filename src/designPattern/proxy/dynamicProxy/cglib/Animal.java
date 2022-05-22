package src.designPattern.proxy.dynamicProxy.cglib;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

/**
 * cglib 代理基于父子类继承
 * 底层 ASM
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
        // MethodInterceptor 拦截器
        // intercept方法的参数依次为：代理类、拦截的方法、参数、方法代理
        enhancer.setCallback((MethodInterceptor) (o, method, objects, methodProxy) -> {
            System.out.println("cglib start to " + method.getName());
            Object obj = method.invoke(animal, objects);
            System.out.println("cglib end to " + method.getName());
            return obj;
        });
        /*
            调用Enhancer.create()时大致流程如下：
                调用createHelper()方法然后进行前置条件检查；
                调用Enhancer内部接口EnhancerKey的newInstance(···)生成唯一key（用于从缓存中获取代理类实例）。
                然后通过super.create(key)获取我们设置的SuperClass对应的代理类实例。

         */
        Animal animalCglib = (Animal) enhancer.create();
        animalCglib.eat();
        System.out.println("=============");
        animalCglib.drink();
    }
}
