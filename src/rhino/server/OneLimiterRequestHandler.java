package src.rhino.server;

import java.util.List;

import src.rhino.onelimiter.OneLimiter;
import src.rhino.onelimiter.OneLimiterStrategy;

/**
 * 统一限流器接口
 * path: /limiter
 * Created by zmz on 2020/12/20.
 */
public class OneLimiterRequestHandler implements RhinoHttpRequestHandler {
    @Override
    public Object handleCommand(String command, String rhinoKey, String... params) {
        switch (command) {
//            case "profiling":
//                return getOneLimiterProfilingData(rhinoKey, null);
            case "mainConfig":
                return getOneLimiterMainConfig(rhinoKey);
            case "entrance":
                return getEntranceConfig(rhinoKey, params);
            default:
                throw new IllegalArgumentException("Unsurpport command: " + command);
        }
    }

    /**
     * 读取限流器主配置
     *
     * @param rhinoKey
     * @return
     */
    private Object getOneLimiterMainConfig(String rhinoKey) {
        OneLimiter limiter = OneLimiter.Factory.getOneLimiter(rhinoKey);
        return limiter == null ? null : limiter.getOneLimiterManager().getOneLimiterConfig();
    }

    /**
     * 读取限流器entrance配置
     *
     * @param rhinoKey
     * @param entrance
     * @return
     */
    private Object getEntranceConfig(String rhinoKey, String[] params) {
        if (params == null || params.length == 0) {
            return null;
        }

        OneLimiter limiter = OneLimiter.Factory.getOneLimiter(rhinoKey);
        if (limiter == null) {
            return null;
        }

        String entrance = params[0];
        List<OneLimiterStrategy> list = limiter.getOneLimiterManager().getStrategyList(entrance, null);
        if (list.size() == 0) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (OneLimiterStrategy strategy : list) {
            builder.append(strategy.toString()).append(",");
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append("]");
        return builder.toString();
    }

    /**
     * 暂不支持读取实时qps
     *
     * @param rhinoKey
     * @param entrance
     * @return
     */
    private Object getOneLimiterProfilingData(String rhinoKey, String entrance) {
        return null;
    }

}
