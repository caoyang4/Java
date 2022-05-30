package src.rhino.fault;

import src.rhino.dispatcher.AbstractEventDispatcher;

/**
 * @author zhanjun on 2017/08/17.
 */
public class FaultInjectEventDispatcher extends AbstractEventDispatcher {

    public FaultInjectEventDispatcher(String rhinoKey) {
        super(rhinoKey);
    }
}
