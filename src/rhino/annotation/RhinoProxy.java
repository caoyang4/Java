package src.rhino.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 使用自定义扫描方式生成注解
 * 优点：该注解可以隔离与业务其它的Spring配置
 * 缺点：与Spring的@Service同时使用会有冲突
 * Created by zmz on 2020/9/9.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE })
public @interface RhinoProxy {
	
	String value() default "";
}
