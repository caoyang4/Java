package src.rhino.annotation;

import static src.rhino.cache.CacheProperties.default_connTimeout;
import static src.rhino.cache.CacheProperties.default_readTimeout;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by zhanjun on 2017/4/21.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD })
public @interface Cache {

    /**
     * cache key
     * @return
     */
    String rhinoKey();


    /**
     * isActive
     * @return
     */
    boolean isActive() default true;

    /**
     * cluster name
     * @return
     */
    String clusterName();

    /**
     * category name
     * @return
     */
    String category();

    /**
     * generate store key
     * @return
     */
    String cacheKeyMethod();

    /**
     * connection time out
     */
    int connTimeout() default default_connTimeout;

    /**
     * read time out
     */
    int readTimeout() default default_readTimeout;
}
