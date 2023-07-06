package src.juc.pratice.limit;

import java.lang.annotation.*;

/**
 * @author caoyang
 * @create 2023-07-06 16:13
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimiter {

    /**
     * 限流key
     */
    String key() default "rate:limiter";
    /**
     * 窗口允许最大请求数
     */
    long maxCount() default 10;

    /**
     * 窗口宽度，单位为ms
     */
    long winWidth() default 1000;

    /**
     * 限流提示语
     */
    String message() default "false";
}

