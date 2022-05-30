package src.rhino.retry;

import src.rhino.dispatcher.AbstractEventDispatcher;

/**
 *
 * @author zhen
 * @date 2019/2/25
 */
public class RetryEventDispatcher extends AbstractEventDispatcher {

    public RetryEventDispatcher(String rhinoKey) {
        super(rhinoKey);
    }

    public static RetryEventDispatcher create(String rhinoKey) {
        return new RetryEventDispatcher(rhinoKey);
    }
}
