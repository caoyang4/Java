package src.rhino.spring;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

/**
 * @author zhanjun
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Configuration
@Import(RhinoBeanDefinitionRegistry.class)
public @interface RhinoConfiguration {
    String packages() default "src,com.meituan,com.sankuai";
    int order() default Ordered.LOWEST_PRECEDENCE;
}
