package src.rhino;

import java.lang.reflect.Method;

import src.cat.Cat;
import src.rhino.cache.Cache;
import src.rhino.fault.FaultInject;
import src.rhino.threadpool.AsyncResult;

/**
 * Created by zhanjun on 2018/8/9.
 */
public class RhinoInnerCommand extends RhinoAbstractCommand<Object> {

    private Object origin;
    private RhinoMethod rhinoMethod;
    private FaultInject faultInject;
    private Object[] args;

    public RhinoInnerCommand(Object origin, RhinoMethod rhinoMethod, Object[] args) {
        super(rhinoMethod.getCircuitBreaker(), rhinoMethod.getThreadPool());
        this.origin = origin;
        this.rhinoMethod = rhinoMethod;
        this.faultInject = rhinoMethod.getFaultInject();
        this.args = args;
    }

    @Override
    public Object run() throws Exception {
        RhinoMethod rhinoMethod = this.rhinoMethod;
        Object[] args = this.args;
        Object origin = this.origin;

        Object mockData = faultInject.inject(rhinoMethod.getReturnType());
        if (mockData != null) {
            return mockData;
        }

        Object result = rhinoMethod.invoke(origin, args);
        if (result instanceof AsyncResult) {
            result = ((AsyncResult) result).invoke();
        }
        try {
            // 执行成功的结果放到缓存中
            rhinoMethod.putInCache(origin, args, result);
        } catch (Throwable e) {
            //缓存操作异常处理，不要影响业务正常流程
            //https://tt.sankuai.com/ticket/detail?id=3801764
            Cat.logError(e);
        }
        return result;
    }

    @Override
    public Object getFallback(Throwable throwable) throws Exception {
        RhinoMethod rhinoMethod = this.rhinoMethod;
        Object[] args = this.args;
        Object origin = this.origin;

        // 尝试从缓存获取降级结果
        Cache cache = rhinoMethod.getCache();
        if (cache != null) {
            Method cacheKeyMethod = rhinoMethod.getCacheKeyMethod();
            if (cacheKeyMethod != null) {
                Object cacheKey = cacheKeyMethod.invoke(origin, args);
                Object value = cache.get(cacheKey);
                if (value != null) {
                    return value;
                }
            }
        }

        // 执行降级逻辑
        Method fallBackMethod = rhinoMethod.getDegradeFallbackMethod();
        if (fallBackMethod == null) {
            return null;
        }

        if (rhinoMethod.isFallbackMethod_has_throwable_param()) {
            Object[] dest = new Object[args.length + 1];
            System.arraycopy(args, 0, dest, 0, args.length);
            dest[args.length] = throwable;
            args = dest;
        }
        return fallBackMethod.invoke(origin, args);
    }
}
