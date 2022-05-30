package src.rhino.fault;

/**
 * Created by zhanjun on 2017/4/25.
 */
public class NoOpFaultInject implements FaultInject {

    @Override
    public boolean isActive(FaultInjectContext context) {
        return false;
    }

    @Override
    public void inject() throws Exception {
        //Do nothing

    }

    @Override
    public void inject(FaultInjectContext context) throws Exception {
        //Do nothing

    }

    @Override
    public <T> T inject(Class<T> returnType) throws Exception {
        return null;
    }

    @Override
    public FaultInjectContext getContext() {
        return null;
    }

    @Override
    public FaultInjectProperties getFaultInjectProperties() {
        return null;
    }
}
