package src.rhino.circuit.listener;

import java.util.List;

import org.springframework.util.CollectionUtils;

import src.rhino.circuit.CircuitBreakerEventData;
import src.rhino.circuit.CircuitBreakerEventType;
import src.rhino.circuit.CircuitBreakerListener;
import src.rhino.dispatcher.RhinoEvent;
import src.rhino.dispatcher.RhinoEventDispatcher;
import src.rhino.log.Logger;
import src.rhino.log.LoggerFactory;

/**
 * @author zhen on 2017/10/30.
 */
public class CircuitBreakerListenerTrigger {

    private static final Logger logger = LoggerFactory.getLogger(CircuitBreakerListenerTrigger.class);
    private String key;
    private RhinoEventDispatcher eventDispatcher;
    private List<CircuitBreakerListener> circuitBreakerListenerList;

    public CircuitBreakerListenerTrigger(String key, RhinoEventDispatcher eventDispatcher) {
        this.key = key;
        this.eventDispatcher = eventDispatcher;
    }

    public void setCircuitBreakerListenerList(List<CircuitBreakerListener> circuitBreakerListenerList) {
        this.circuitBreakerListenerList = circuitBreakerListenerList;
    }

    /**
     * @param eventType
     * @param eventData
     */
    public void circuitBreakerOpened(CircuitBreakerEventType eventType, CircuitBreakerEventData eventData) {
        if (!CollectionUtils.isEmpty(circuitBreakerListenerList)) {
            CircuitBreakerListenerContext context = new CircuitBreakerListenerContext(key, eventType, eventData);
            for (CircuitBreakerListener circuitBreakerListener : circuitBreakerListenerList) {
                try {
                    circuitBreakerListener.circuitBreakerOpened(context);
                } catch (Exception e) {
                    logger.error("circuitBreakerOpened", e);
                }
            }
        }
        eventDispatcher.dispatchEvent(new RhinoEvent(eventType, eventData));
    }

    /**
     * @param eventType
     * @param eventData
     */
    public void circuitBreakerClosed(CircuitBreakerEventType eventType, CircuitBreakerEventData eventData) {
        if (!CollectionUtils.isEmpty(circuitBreakerListenerList)) {
            CircuitBreakerListenerContext context = new CircuitBreakerListenerContext(key, eventType, eventData);
            for (CircuitBreakerListener circuitBreakerListener : circuitBreakerListenerList) {
                try {
                    circuitBreakerListener.circuitBreakerClosed(context);
                } catch (Exception e) {
                    logger.error("circuitBreakerClosed", e);
                }
            }
        }
        eventDispatcher.dispatchEvent(new RhinoEvent(eventType, eventData));
    }
}
