package src.basis.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 不能使用关键字extends来继承某个@interface，
 * 注解在编译后，编译器会自动继承java.lang.annotation.Annotation接口.
 * @author caoyang
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface MyAnnotation {
    // 属性，看着像方法，实际是属性
    String name();

    double version() default 1.0;

    // 默认属性值
    String color() default "red";

    String description() default "";
}

