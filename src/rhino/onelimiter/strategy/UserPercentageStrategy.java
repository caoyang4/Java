package src.rhino.onelimiter.strategy;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.mysql.cj.util.StringUtils;
import org.springframework.util.CollectionUtils;

import src.rhino.onelimiter.ExecuteResult;
import src.rhino.onelimiter.OneLimiterConstants;
import src.rhino.onelimiter.OneLimiterStrategy;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

/**
 * 用户百分比策略
 * Created by zhanjun on 2018/5/30.
 */
public class UserPercentageStrategy implements LimiterStrategy {

    private static HashFunction hashFunction = Hashing.murmur3_128();
    private int code;
    private String msg;

    /**
     * 拒绝百分比
     */
    private int rejectPercent;

    /**
     * 白名单的接口不能拒绝
     */
    private WhiteBlackProcessor whiteProcessor;

    /**
     * 空文案列表，被拒绝时，返回消息为空
     * 外卖特殊需求，如果拒绝返回消息，APP启动时可能出现弹框
     */
    private WhiteBlackProcessor emptyMsgProcessor;

    public UserPercentageStrategy(OneLimiterStrategy strategy) {
        this.code = strategy.getCode();
        this.msg = strategy.getMsg();
        this.rejectPercent = strategy.getThreshold();
        this.whiteProcessor = new WhiteBlackProcessor(strategy.getWhiteList());
        this.emptyMsgProcessor = new WhiteBlackProcessor(strategy.getBlackList());
    }

    /**
     * 以百分比为概率随机拒绝用户请求
     *
     * @param entrance  资源标识
     * @param reqParams 通过uuid来区分用户
     * @param tokens    无效参数
     * @return
     */
    @Override
    public ExecuteResult execute(String entrance, Map<String, String> reqParams, int tokens) {

        // 如果条件不满足，继续执行下一个策略
        if (rejectPercent <= 0 || CollectionUtils.isEmpty(reqParams)) {
            return ExecuteResult.CONTINUE;
        }

        String userKey = reqParams.get(OneLimiterConstants.UUID);
        if (StringUtils.isNullOrEmpty(userKey)) {
            userKey = reqParams.get(OneLimiterConstants.IP);
            if (StringUtils.isNullOrEmpty(userKey)) {
                return ExecuteResult.CONTINUE;
            }
        }

        // 如果在白名单中，继续执行下一个策略
        if (whiteProcessor.contains(entrance)) {
            return ExecuteResult.CONTINUE;
        }

        // userKey拼接随机因子，hash取模，模值∈[0,99]
        long days = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis());
        userKey = new StringBuilder(userKey).append(days).toString();
        int userKeyModulusValue = Math.abs(hashFunction.newHasher().putString(userKey, Charset.defaultCharset()).hash().asInt() % 100);

        // 模值命中(rejectPercent,100]区间、或接口在白名单中，放过请求；[0,rejectPercent]区间拒绝请求
        if (userKeyModulusValue > rejectPercent) {
            return ExecuteResult.CONTINUE;
        }

        // 请求被限流
        // 若接口在返回空文案的url集合中，则拦截文案置空
        String msg = emptyMsgProcessor.contains(entrance) ? StringUtils.EMPTY : this.msg;
        return ExecuteResult.createRejectResult(code, msg);
    }
}
