package java.lang;

import java.lang.annotation.*;
// 函数式接口
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FunctionalInterface {}
