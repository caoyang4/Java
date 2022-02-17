package src.spring.ioc;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class MyFileSystemXmlApplicationContext extends FileSystemXmlApplicationContext {

    public MyFileSystemXmlApplicationContext(String configLocation) throws BeansException {
        super(configLocation);
    }

    /**
     * 用户定制化入口
     */
    @Override
    protected void initPropertySources() {
        // For subclasses: do nothing by default.
        System.out.println("DIY spring initPropertySources");
    }

    /**
     * 定制 bean 工厂属性
     * @param beanFactory
     */
    @Override
    protected void customizeBeanFactory(DefaultListableBeanFactory beanFactory) {
        // 是否允许覆盖 bean 的定义信息
        super.setAllowBeanDefinitionOverriding(true);
        // 是否允许循环引用
        super.setAllowCircularReferences(true);
        super.customizeBeanFactory(beanFactory);
    }
}
