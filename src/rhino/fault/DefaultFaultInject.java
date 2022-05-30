
package src.rhino.fault;

import java.util.concurrent.atomic.AtomicInteger;

import src.rhino.fault.type.FaultSimulator;
import src.rhino.fault.type.FaultType;

/**
 * @author zhanjun on 2017/4/25.
 */
public class DefaultFaultInject implements FaultInject {

    private volatile FaultSimulator faultSimulator = FaultSimulator.Factory.empty();
    private FaultInjectProperties faultInjectProperties;
    private AtomicInteger faultType = new AtomicInteger(0);

    public DefaultFaultInject(String key, FaultInjectProperties faultInjectProperties) {
        this.faultInjectProperties = faultInjectProperties;
    }

    @Override
    public boolean isActive(FaultInjectContext context) {
        return faultInjectProperties.getIsActive(context);
    }

    @Override
    public void inject() throws Exception {
        inject((FaultInjectContext) null);
    }

    @Override
    public void inject(FaultInjectContext context) throws Exception {
        if (faultInjectProperties.getIsActive(context)) {
            int oldFaultType = faultType.get();
            int newFaultType = faultInjectProperties.getType();
            if (oldFaultType != newFaultType && faultType.compareAndSet(oldFaultType, newFaultType)) {
                faultSimulator = FaultSimulator.Factory.create(faultInjectProperties);
            }
            faultSimulator.simulate();
        }
    }

    @Override
    public <T> T inject(Class<T> returnType) throws Exception {
        if (faultInjectProperties.getIsActive()) {
            int oldFaultType = faultType.get();
            int newFaultType = faultInjectProperties.getType();
            if (oldFaultType != newFaultType && faultType.compareAndSet(oldFaultType, newFaultType)) {
                faultSimulator = FaultSimulator.Factory.create(faultInjectProperties);
            }
            return faultSimulator.simulate(returnType);
        }
        return null;
    }

    @Override
    public FaultInjectContext getContext() {
        FaultInjectContext faultInjectContext = FaultInjectContext.create();
        faultInjectContext.setDelay(FaultType.get(faultInjectProperties.getType()).isDelay());
        faultInjectContext.setDelayTime(faultInjectProperties.getMaxDelay());
        return faultInjectContext;
    }

    @Override
    public FaultInjectProperties getFaultInjectProperties() {
        return faultInjectProperties;
    }
}
