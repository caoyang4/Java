package src.rhino.spring;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;

import src.rhino.RhinoServiceFactory;
import src.rhino.annotation.Rhino;
import src.rhino.annotation.RhinoProxy;
import src.rhino.log.Logger;
import src.rhino.log.LoggerFactory;
import src.rhino.util.ClassUtils;

/**
 * Created by zhanjun on 2017/4/26.
 */
public class RhinoBean implements BeanPostProcessor, BeanFactoryPostProcessor, ApplicationContextAware, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(RhinoBean.class);
    private String annotationPackage = "src,com.meituan,com.sankuai";
    private ApplicationContext applicationContext;
    private int order = Ordered.LOWEST_PRECEDENCE;
    private static final Pattern COMMA_SPLIT_PATTERN = Pattern.compile("\\s*[,]+\\s*");

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = AopUtils.getTargetClass(bean);
        if (beanClass == null) {
            return bean;
        }

        Rhino rhino = beanClass.getAnnotation(Rhino.class);
        RhinoProxy rhinoProxy = beanClass.getAnnotation(RhinoProxy.class);
        if (rhino != null || rhinoProxy != null) {
            try {
                return RhinoServiceFactory.create(beanClass, bean);
            } catch (Exception e) {
                throw new FatalBeanException(e.toString());
            }
        }
        return bean;
    }

    public String getPackage() {
        return annotationPackage;
    }

    public void setPackage(String annotationPackage) {
        this.annotationPackage = annotationPackage;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    /**
     * 自定义扫描Spring Bean
     * @param beanFactory
     * @throws BeansException
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (annotationPackage == null || annotationPackage.length() == 0) {
            return;
        }
        if (beanFactory instanceof BeanDefinitionRegistry) {
            try {
                // init scanner
                Class<?> scannerClass = ClassUtils.loadClass("org.springframework.context.annotation.ClassPathBeanDefinitionScanner");
                Object scanner = scannerClass.getConstructor(
                        new Class<?>[]{BeanDefinitionRegistry.class, boolean.class}).newInstance(
                        new Object[]{(BeanDefinitionRegistry) beanFactory, false});
                // add filter
                Class<?> filterClass = ClassUtils.loadClass("org.springframework.core.type.filter.AnnotationTypeFilter");
                Object filter = filterClass.getConstructor(Class.class).newInstance(RhinoProxy.class);
                Method addIncludeFilter = scannerClass.getMethod("addIncludeFilter", ClassUtils.loadClass("org.springframework.core.type.filter.TypeFilter"));
                addIncludeFilter.invoke(scanner, filter);
                // scan packages
                String[] packages = COMMA_SPLIT_PATTERN.split(annotationPackage);
                Method scan = scannerClass.getMethod("scan", new Class<?>[]{String[].class});
                scan.invoke(scanner, new Object[]{packages});
            } catch (Throwable e) {
                //ignore exception
            }
        }
    }
}
