package src.rhino.retry.delay;

import src.rhino.config.PropertyChangedListener;
import src.rhino.retry.RetryContext;
import src.rhino.retry.RetryProperties;
import src.rhino.retry.RetryPropertiesBean;

/**
 * Created by zhen on 2019/2/20.
 */
public class BackOffStrategyProxy extends PropertyChangedListener<RetryPropertiesBean> implements BackOffStrategy {


    private volatile BackOffStrategy backOffStrategy;
    private RetryProperties retryProperties;

    public BackOffStrategyProxy(BackOffStrategy backOffStrategy, RetryProperties retryProperties) {
        this.backOffStrategy = backOffStrategy;
        this.retryProperties = retryProperties;
        this.retryProperties.addPropertyChangedListener(this);
    }

    @Override
    public void trigger(RetryPropertiesBean oldProperty, RetryPropertiesBean newProperty) {
        if (oldProperty.getDelayStrategy() != newProperty.getDelayStrategy()
                || Type.getType(newProperty.getDelayStrategy()).isPropertiesChanged(oldProperty, newProperty)) {
            backOffStrategy = Type.getType(newProperty.getDelayStrategy()).create(retryProperties);
        }
    }

    @Override
    public void backOff(RetryContext retryContext) {
        backOffStrategy.backOff(retryContext);
    }
}
