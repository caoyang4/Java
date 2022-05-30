
package src.rhino.fault.type;

import java.io.IOException;

import src.rhino.exception.RhinoIgnoreException;
import src.rhino.fault.FaultInjectEventType;
import src.rhino.fault.FaultInjectProperties;

/**
 * @author wanghao on 17/11/20.
 */
public class MockDataSimulator extends BasicFaultSimulator {

    public MockDataSimulator(FaultInjectProperties requestInjectProperties) {
        super(requestInjectProperties, FaultInjectEventType.MOCK);
    }

    @Override
    public <T> T doSimulate(Class<T> returnType) throws Exception {
        if (returnType == null) {
            return null;
        }
        try {
            return requestInjectProperties.getMockValue(returnType);
        } catch (IOException ioe) {
            throw new RhinoIgnoreException(ioe);
        }
    }
}
