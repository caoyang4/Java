package src.rhino.fault.type;

import src.rhino.fault.FaultInjectProperties;

/**
 * @author zhanjun on 2017/6/30.
 */
public class NoOpFaultSimulator extends BasicFaultSimulator {

    public NoOpFaultSimulator(FaultInjectProperties requestInjectProperties) {
        super(requestInjectProperties, null);
    }

    @Override
    public <T> T doSimulate(Class<T> returnType) throws Exception {
        return null;
    }
}
