package src.rhino.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import src.rhino.limit.LimiterHandlerEnum;
import src.rhino.limit.RequestLimiterProperties;

/**
 * Created by zhanjun on 2017/4/21.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD })
public @interface RateLimit {

    String rhinoKey();
    boolean isActive() default false;
    int rate() default RequestLimiterProperties.default_rate;
    LimiterHandlerEnum strategy() default LimiterHandlerEnum.EXCEPTION;
    long timeoutInMilliseconds() default RequestLimiterProperties.default_timeoutInMilliseconds;
    String fallBackMethod()  default "";
}
