package src.rhino.service.command;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.mysql.cj.util.StringUtils;

import src.rhino.RhinoType;
import src.rhino.onelimiter.OneLimiter;
import src.rhino.onelimiter.OneLimiterManager;
import src.rhino.onelimiter.OneLimiterStrategy;
import src.rhino.onelimiter.OneLimiterStrategyEnum;
import src.rhino.onelimiter.alarm.OneLimiterQpsEntity;
import src.rhino.util.SerializerUtils;
import com.google.common.collect.Maps;

/**
 * Created by zhen on 2018/11/29.
 */
public class OneLimiterCommand implements Command {

    private CommandProperties commandProperties;

    public OneLimiterCommand(CommandProperties commandProperties) {
        this.commandProperties = commandProperties;
    }

    @Override
    public String getName() {
        return RhinoType.get(commandProperties.getRhinoType());
    }

    @Override
    public Object run() {
        String rhinoKey = commandProperties.getRhinoKey();
        OneLimiter target = OneLimiter.Factory.getOneLimiter(rhinoKey);
        if (target == null) {
            throw new IllegalArgumentException("invalid rhino key or the target oneLimiter has not initialed");
        }
        Map<String, String> params = commandProperties.getParams();
        int operationType = commandProperties.getOperationType();
        switch (operationType) {
            case 91:
                return getTargetStrategyInfo(target, params);
            default:
                return null;
        }
    }

    /**
     *
     * @param oneLimiter
     * @param params
     * @return
     */
    private Object getTargetStrategyInfo(OneLimiter oneLimiter, Map<String, String> params) {
        String entrance = params.get("entrance");
        OneLimiterManager oneLimiterManager = oneLimiter.getOneLimiterManager();
        String strategyEnum = params.get("strategyEnum");
        if (StringUtils.isNullOrEmpty(strategyEnum)) {
            return null;
        }
        OneLimiterStrategy strategy = null;
        if (StringUtils.isNullOrEmpty(entrance)) {
            if (OneLimiterStrategyEnum.SINGLE_VM_QPS_ALL.getName().equals(strategyEnum)) {
                strategy = oneLimiterManager.getOneLimiterConfig().getSingleVmQps();
            }
        } else {
            Map<String, String> paramsMap = null;
            String runParams = params.get("params");
            if (StringUtils.isNullOrEmpty(runParams)) {
                try {
                    paramsMap = SerializerUtils.read(runParams, Map.class);
                } catch (IOException e) {
                    //ignore exception
                }
            }
            List<OneLimiterStrategy> oneLimiterStrategyList = oneLimiterManager.getStrategyList(entrance, paramsMap);
            for (OneLimiterStrategy oneLimiterStrategy : oneLimiterStrategyList) {
                if (oneLimiterStrategy.getStrategyEnum().getName().equals(strategyEnum)) {
                    strategy = oneLimiterStrategy;
                    break;
                }
            }
        }
        if (strategy != null) {
            long now = System.currentTimeMillis();
            List<OneLimiterQpsEntity> entities = strategy.getLimiterAlarm().getOneLimiterQpsMetric().getQpsSummary(now);
            Map<String, Object> result = Maps.newHashMap();
            result.put("strategy", strategy.toString());
            result.put("qps", entities);
            return result;
        }
        return null;
    }
}
