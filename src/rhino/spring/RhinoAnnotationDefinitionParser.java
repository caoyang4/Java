package src.rhino.spring;

import java.util.concurrent.atomic.AtomicInteger;

import com.mysql.cj.util.StringUtils;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Created by zhanjun on 2017/4/26.
 */
public class RhinoAnnotationDefinitionParser implements BeanDefinitionParser {

    public static final AtomicInteger counter = new AtomicInteger();

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        beanDefinition.setLazyInit(false);
        beanDefinition.setBeanClass(RhinoBean.class);
        String id = element.getAttribute("id");
        if (StringUtils.isNullOrEmpty(id)) {
            id = "rhino_" + counter.incrementAndGet();
        }

        MutablePropertyValues properties = beanDefinition.getPropertyValues();
        if (element.hasAttribute("package")) {
            properties.addPropertyValue("package", element.getAttribute("package"));
        }
        if (element.hasAttribute("order")) {
            properties.addPropertyValue("order", Integer.valueOf(element.getAttribute("order")));
        }
        parserContext.getRegistry().registerBeanDefinition(id, beanDefinition);

        return beanDefinition;
    }
}
