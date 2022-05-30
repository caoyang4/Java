package src.rhino.dispatcher;

import java.util.Arrays;
import java.util.List;

/**
 * @author zhanjun on 2017/08/17.
 */
public abstract class AbstractEventDispatcher implements RhinoEventDispatcher {

    private static List<RhinoEventListener> eventListeners = Arrays.asList(CatEventListener.INSTANCE, NotifyEventListener.INSTANCE);
    private String rhinoKey;

    public AbstractEventDispatcher(String rhinoKey) {
        this.rhinoKey = rhinoKey;
    }

    @Override
    public void dispatchEvent(RhinoEvent event) {
        for (RhinoEventListener listener : eventListeners) {
            listener.onEvent(rhinoKey, event);
        }
    }
}
