package src.rhino.fault.type;

import java.util.concurrent.ThreadLocalRandom;

import src.rhino.dispatcher.RhinoEvent;
import src.rhino.dispatcher.RhinoEventDispatcher;
import src.rhino.exception.RhinoIgnoreException;
import src.rhino.fault.FaultInjectEventDispatcher;
import src.rhino.fault.FaultInjectEventType;
import src.rhino.fault.FaultInjectProperties;
import src.rhino.log.Logger;
import src.rhino.log.LoggerFactory;

/**
 * @author zhanjun on 2017/6/30.
 */
public abstract class BasicFaultSimulator implements FaultSimulator {

    private final static Logger logger = LoggerFactory.getLogger(BasicFaultSimulator.class);

    protected FaultInjectProperties requestInjectProperties;
    protected RhinoEventDispatcher eventDispatcher;
    protected FaultInjectEventType eventType;

    public BasicFaultSimulator(FaultInjectProperties requestInjectProperties, FaultInjectEventType eventType) {
        this.requestInjectProperties = requestInjectProperties;
        this.eventType = eventType;
        this.eventDispatcher = new FaultInjectEventDispatcher(requestInjectProperties.getRhinoKey());
    }

    @Override
    public <T> T simulate() throws Exception {
        return simulate(null);
    }

    @Override
    public <T> T simulate(Class<T> returnType) throws Exception {
        float sampleRate = requestInjectProperties.getSampleRate();
        if (sampleRate > 0 && ThreadLocalRandom.current().nextInt(10000) <= sampleRate * 100) {
            eventDispatcher.dispatchEvent(new RhinoEvent(eventType));
            try {
                return doSimulate(returnType);
            } catch (RhinoIgnoreException e) {
                // 如果配置的异常类有问题，就忽略，避免引起不必要的问题
                // ignore exception
                logger.warn("fault simulate failed", e);
                return null;
            }
        }
        return null;
    }

    /**
     * do simulate
     *
     * @param returnType
     * @param <T>
     * @return
     * @throws Exception
     */
    abstract <T> T doSimulate(Class<T> returnType) throws Exception;

}
