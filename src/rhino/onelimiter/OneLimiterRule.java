package src.rhino.onelimiter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.util.CollectionUtils;

/**
 * Created by zhanjun on 2018/4/13.
 */
public class OneLimiterRule {

	private long entranceId;
    private String entrance;
    private boolean regex;
    private List<OneLimiterStrategy> strategyList;
    private List<OneLimiterStrategy> testStrategyList;

    public void init(String rhinoKey) {
        initTestStrategyList(rhinoKey);
        initNormalStrategyList(rhinoKey);
        mergeTestStrategy();
    }

    private void initTestStrategyList(String rhinoKey) {
        testStrategyList = new ArrayList<>();

        if (CollectionUtils.isEmpty(strategyList)) {
            return;
        }

        //如果配置了压测策略，就拷贝压测参数，否则拷贝正常参数
        for (OneLimiterStrategy normalStrategy : strategyList) {
            OneLimiterStrategy testStrategy;
            if (normalStrategy.isTestConfiged()) {
                testStrategy = OneLimiterStrategy.copy(normalStrategy, true);
            } else {
                testStrategy = OneLimiterStrategy.copy(normalStrategy, false);
            }
            testStrategy.setTestStrategy(true);
            testStrategyList.add(testStrategy);
        }

        //排序后初始化
        sortStrategyList(testStrategyList);
        for (OneLimiterStrategy strategy : testStrategyList) {
            strategy.setEntrance(entrance);
            strategy.setEntranceId(entranceId);
            strategy.setTest(true);
            strategy.init(rhinoKey);
        }
    }

    private void initNormalStrategyList(String rhinoKey) {
        if (CollectionUtils.isEmpty(strategyList)) {
            return;
        }

        //排序后初始化
        sortStrategyList(strategyList);
        for (OneLimiterStrategy strategy : strategyList) {
            strategy.setEntrance(entrance);
            strategy.setEntranceId(entranceId);
            strategy.init(rhinoKey);
        }
    }
    
    //将合并计数的压测策略添加到正常策略中
    private void mergeTestStrategy() {
    	for (OneLimiterStrategy strategy : testStrategyList) {
            if(strategy.isTestMergeNormal()) {
            	strategyList.add(strategy);
            }
        }
    }

    /**
     * 对限流策略，根据策略类型、是否有参数进行排序
     *
     * @param strategyList
     */
    private void sortStrategyList(List<OneLimiterStrategy> strategyList) {
        Collections.sort(strategyList, new Comparator<OneLimiterStrategy>() {
            @Override
            public int compare(OneLimiterStrategy strategy1, OneLimiterStrategy strategy2) {
                int type_order1 = strategy1.getStrategyEnum().getOrder();
                int type_order2 = strategy2.getStrategyEnum().getOrder();

                if (type_order1 == type_order2) {
                    //如类型相同，参数不为空的策略优先级为0，参数为空的策略优先级为1
                    int param_order1 = strategy1.getParamRules() != null && strategy1.getParamRules().isEmpty() ? 1 : 0;
                    int param_order2 = strategy2.getParamRules() != null && strategy2.getParamRules().isEmpty() ? 1 : 0;
                    return param_order1 - param_order2;
                } else {
                    //优先保证策略类型优先级
                    return type_order1 - type_order2;
                }
            }
        });
    }

    public long getEntranceId() {
        return entranceId;
    }

    public void setEntranceId(long entranceId) {
        this.entranceId = entranceId;
    }

    public String getEntrance() {
        return entrance;
    }

    public void setEntrance(String entrance) {
        this.entrance = entrance;
    }

    public boolean isRegex() {
        return regex;
    }

    public void setRegex(boolean regex) {
        this.regex = regex;
    }

    public List<OneLimiterStrategy> getStrategyList() {
        return strategyList;
    }

    public void setStrategyList(List<OneLimiterStrategy> strategyList) {
        this.strategyList = strategyList;
    }

    public void setTestStrategyList(List<OneLimiterStrategy> testStrategyList) {
        this.testStrategyList = testStrategyList;
    }

    public List<OneLimiterStrategy> getTestStrategyList() {
        return testStrategyList;
    }

    /**
     * 根据压测标识获取entrance限流策略
     *
     * @param isTestRequest
     * @return
     */
    public List<OneLimiterStrategy> getStrategyList(boolean isTestRequest) {
        return isTestRequest ? testStrategyList : strategyList;
    }
}
