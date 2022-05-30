package src.rhino.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Created by zhanjun on 2017/4/26.
 */
public class CommonNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("annotation", new RhinoAnnotationDefinitionParser());
    }
}
