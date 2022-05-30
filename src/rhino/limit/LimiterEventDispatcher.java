package src.rhino.limit;

import src.rhino.dispatcher.AbstractEventDispatcher;

/**
 * Created by zhanjun on 2017/08/17.
 */
public class LimiterEventDispatcher extends AbstractEventDispatcher {

    public LimiterEventDispatcher(String rhinoKey) {
        super(rhinoKey);
    }
}
