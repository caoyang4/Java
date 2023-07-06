package src.juc.pratice.limit;

import com.alibaba.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author caoyang
 * @create 2023-07-06 16:14
 */
@Component
@Aspect
@Slf4j
public class RateLimitAspect {

    @Resource
    StringRedisTemplate stringRedisTemplate;

    private DefaultRedisScript<Long> getRedisScript;

    @PostConstruct
    public void init() {
        getRedisScript = new DefaultRedisScript<>();
        getRedisScript.setResultType(Long.class);
        getRedisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("window.lua")));
        log.info("RateLimiter[分布式限流处理器]脚本加载完成");
    }

    @Pointcut("@annotation(src.juc.pratice.limit.RateLimiter)")
    public void rateLimiter() {}

    @Around("@annotation(rateLimiter)")
    public Object around(ProceedingJoinPoint proceedingJoinPoint, RateLimiter rateLimiter) throws Throwable {
        if (log.isDebugEnabled()) {
            log.debug("RateLimiter[分布式限流处理器]开始执行限流操作");
        }
        Signature signature = proceedingJoinPoint.getSignature();
        if (!(signature instanceof MethodSignature)) {
            throw new IllegalArgumentException("the Annotation @RateLimiter must used on method!");
        }

        /*
         *  获取注解参数
         *  限流模块key
         *  按业务需求定制化处理
         *  这里用tenantId作为key的一部分，实现分租户限流的目的
         *
         */
        String limitKey = rateLimiter.key();
        Preconditions.checkNotNull(limitKey);
        /*时间窗口内可接受的最大请求次数*/
        Long maxCount = rateLimiter.maxCount();
        /*时间窗口宽度*/
        Long winWidth = rateLimiter.winWidth();
        if (log.isDebugEnabled()) {
            log.debug("RateLimiterHandler[分布式限流处理器]参数值为-maxCount={},winWidth={}", maxCount, winWidth);
        }
        // 限流提示语
        String message = rateLimiter.message();
        if (StringUtils.isBlank(message)) {
            message = "false";
        }
        // 执行Lua脚本
        List<String> keyList = new ArrayList();
        // 设置key值为注解中的值
        keyList.add(limitKey);
        // 调用脚本并执行
        log.info("keyList={}, maxCount={}, winWidth={}", keyList, maxCount, winWidth);
        Long result = stringRedisTemplate.execute(getRedisScript, keyList, maxCount.toString(), winWidth.toString());
        if (Objects.isNull(result) || result == 0) {
            String msg = "由于超过窗口宽度=" + winWidth + "-允许" + limitKey + "的请求次数=" + maxCount + "[触发限流]";
            log.debug(msg);
            throw new Exception("request limit, failure...");
        }
        if (log.isDebugEnabled()) {
            log.debug("RateLimiterHandler[分布式限流处理器]限流执行结果-result={},请求[正常]响应", result);
        }
        return proceedingJoinPoint.proceed();
    }
}

