package src.rhino.fault.type;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import src.rhino.fault.FaultInjectEventType;
import src.rhino.fault.FaultInjectProperties;

/**
 * @author zhanjun on 2017/6/30.
 */
public class DelayFaultSimulator extends BasicFaultSimulator {

    private Random delayRandom = new Random();

    public DelayFaultSimulator(FaultInjectProperties requestInjectProperties) {
        super(requestInjectProperties, FaultInjectEventType.DELAY);
    }

    @Override
    public <T> T doSimulate(Class<T> returnType) throws Exception {
        int maxDelay = requestInjectProperties.getMaxDelay();
        int delay = requestInjectProperties.getIsRandomDelay() ? delayRandom.nextInt(maxDelay) : maxDelay;
        TimeUnit.MILLISECONDS.sleep(delay);
        return null;
    }
}
