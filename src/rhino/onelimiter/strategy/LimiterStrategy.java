package src.rhino.onelimiter.strategy;

import java.util.Map;

import src.rhino.onelimiter.ExecuteResult;

/**
 * 限流策略定义接口
 * Created by zhanjun on 2018/4/13.
 */
public interface LimiterStrategy {
    ExecuteResult execute(String entrance, Map<String, String> reqParams, int tokens);
}
