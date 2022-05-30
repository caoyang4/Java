package src.rhino.onelimiter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import src.rhino.util.MtraceUtils;

/**
 * 统一限流器
 * Created by zhanjun on 2018/4/13.
 */
public class DefaultOneLimiter implements OneLimiter {

    private String rhinoKey;
    private OneLimiterManager oneLimiterManager;

    public DefaultOneLimiter(String rhinoKey) {
        this.rhinoKey = rhinoKey;
        this.oneLimiterManager = new OneLimiterManager(rhinoKey);
    }

    /**
     * 简单限流入口：默认消费一个令牌，不指定参数匹配
     * @param entrance 资源标识
     * @return
     */
    @Override
    public LimitResult run(String entrance) {
        return run(entrance, null, 1);
    }

    /**
     * 带参数匹配的限流入口
     * @param entrance
     * @param reqParams 自定义匹配参数
     * @return
     */
    public LimitResult run(String entrance, Map<String, String> reqParams){
        return run(entrance, reqParams, 1);
    }


    /**
     *
     *
     * @param path
     * @param reqParams
     * @return
     */
    @Override
    public LimitResult run(String entrance, Map<String, String> reqParams, int tokens) {
        if(tokens < 1){
            throw new IllegalArgumentException("Tokens required for the request can't less than 1");
        }

        return doRun(entrance, reqParams, tokens);
    }

    private LimitResult doRun(String entrance, Map<String, String> reqParams, int tokens){
        LimitResult limitResult = new LimitResult();

        // 判断总开关和灰度情况
        if (!oneLimiterManager.isActive()) {
            limitResult.setMsg("流控功能未启用");
            return limitResult;
        }

        // 判断压测流量
        boolean isTestRequest = MtraceUtils.isTest();
        if (oneLimiterManager.isTestRequestIgnored() && isTestRequest) {
            limitResult.setMsg("压测流量被忽略");
            return limitResult;
        }

        //匹配参数规则并执行
        List<OneLimiterStrategy> strategyList = oneLimiterManager.getStrategyList(entrance, reqParams, isTestRequest);
        Iterator<OneLimiterStrategy> iterator = strategyList.iterator();
        ExecuteResult executeResult = null;
        OneLimiterStrategy currentStrategy = null;
        while (iterator.hasNext()) {
            currentStrategy = iterator.next();
            executeResult = currentStrategy.execute(entrance, reqParams, tokens);
            // 一般是参数策略没命中
            if (executeResult == null) {
                continue;
            }

            // 触发预警
            if(executeResult.getStatus().isWarn()){
                List<OneLimiterStrategyEnum> warnStrategies = limitResult.getWarnStrategies();
                if(warnStrategies == null){
                    warnStrategies = new ArrayList<>();
                    limitResult.setWarnStrategies(warnStrategies);
                }
                warnStrategies.add(currentStrategy.getStrategyEnum());
            }

            // 黑名单或者拒绝
            if (executeResult.getStatus().isTerminal()) {
                break;
            }
        }

        if (oneLimiterManager.isDebug()) {
            limitResult.setMsg("开启DEBUG模式，只告警，不限流");
            return limitResult;
        }

        if (executeResult != null && executeResult.getStatus().isReject()) {
            limitResult.setReject();
            limitResult.setCode(executeResult.getCode());
            limitResult.setMsg(executeResult.getMsg());
            limitResult.setStrategyEnum(currentStrategy.getStrategyEnum());
        }else{
            System.out.println("Rhino.OneLimiter.Log: result: pass");
        }

        return limitResult;
    }

    @Override
    public OneLimiterManager getOneLimiterManager() {
        return oneLimiterManager;
    }

}
