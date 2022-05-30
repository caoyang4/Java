package src.rhino.spring;

import java.util.Map;

import com.mysql.cj.util.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotationMetadata;

import src.rhino.log.Logger;
import src.rhino.log.LoggerFactory;

/**
 * @author zhanjun
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RhinoBeanDefinitionRegistry implements ImportBeanDefinitionRegistrar, BeanFactoryAware {

    private static final Logger logger = LoggerFactory.getLogger(RhinoBeanDefinitionRegistry.class);
    private ConfigurableListableBeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (beanFactory instanceof ConfigurableListableBeanFactory) {
            this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
        }
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        if (this.beanFactory == null) {
            logger.warn("It is better to use RhinoConfiguration without any xml configuration file, injected by @RhinoConfiguration or Spring Boot purely");
        }
        registerAnnotationBean(importingClassMetadata, registry);
    }

    private void registerAnnotationBean(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        beanDefinition.setLazyInit(false);
        beanDefinition.setBeanClass(RhinoBean.class);

        boolean isAnnotated = importingClassMetadata.isAnnotated(RhinoConfiguration.class.getName());

        if (isAnnotated) {
            Map<String, Object> attributes = importingClassMetadata.getAnnotationAttributes(RhinoConfiguration.class.getName());
            if (attributes != null && !attributes.isEmpty()) {
                String packages = (String) attributes.get("packages");
                if (StringUtils.isNullOrEmpty(packages)) {
                    beanDefinition.getPropertyValues().add("package", packages);
                }
                beanDefinition.getPropertyValues().add("order", attributes.get("order"));
            }
        }

        BeanDefinitionHolder holder = new BeanDefinitionHolder(beanDefinition, "rhino_" + RhinoAnnotationDefinitionParser.counter.incrementAndGet());
        BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);
    }
}