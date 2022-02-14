package src.spring.ioc.factoryBean;

import org.springframework.beans.factory.FactoryBean;

/**
 * @author caoyang
 */
public class BeanFactoryDemo implements FactoryBean<Cat> {
    @Override
    public Cat getObject() throws Exception {
        Cat cat = new Cat();
        System.out.println("===cat miao miao miao===");
        return cat;
    }

    @Override
    public Class<?> getObjectType() {
        return Cat.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public static void main(String[] args) throws Exception {
        BeanFactoryDemo demo = new BeanFactoryDemo();
        Cat cat = demo.getObject();
        System.out.println(cat);

    }
}
