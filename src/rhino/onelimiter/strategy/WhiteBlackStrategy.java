package src.rhino.onelimiter.strategy;

import java.util.Map;

import src.rhino.onelimiter.ExecuteResult;
import src.rhino.onelimiter.OneLimiterStrategy;

/**
 * 黑白名单策略
 * Created by zhanjun on 2018/5/30.
 */
public class WhiteBlackStrategy implements LimiterStrategy {

    /**
     * 白名单处理器
     */
    private WhiteBlackProcessor whiteProcessor;

    /**
     * 黑名单处理器
     */
    private WhiteBlackProcessor blackProcessor;

    private int code;

    private String msg;

    public WhiteBlackStrategy(OneLimiterStrategy strategy) {
        this.code = strategy.getCode();
        this.msg = strategy.getMsg();
        this.whiteProcessor = new WhiteBlackProcessor(strategy.getWhiteList());
        this.blackProcessor = new WhiteBlackProcessor(strategy.getBlackList());
    }

    /**
     * 黑名单：列表中entrance直接拒绝
     * 白名单：列表中entrance直接通过
     *
     * @param entrance  资源标识
     * @param reqParams 无效参数
     * @param tokens    无效参数
     * @return
     */
    @Override
    public ExecuteResult execute(String entrance, Map<String, String> reqParams, int tokens) {

        // 先判断黑名单
        if (blackProcessor.contains(entrance)) {
            return ExecuteResult.createRejectResult(code, msg);
        }

        if (whiteProcessor.contains(entrance)) {
            return ExecuteResult.DIRECT_PASS;
        }

        // 不在白名单、黑名单中
        return ExecuteResult.CONTINUE;
    }
}
