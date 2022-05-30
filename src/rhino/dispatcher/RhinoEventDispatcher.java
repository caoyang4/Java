package src.rhino.dispatcher;

/**
 * @author zhanjun on 2017/08/17.
 */
public interface RhinoEventDispatcher {

    /**
     * dispatch rhino event
     * @param event
     */
    void dispatchEvent(RhinoEvent event);
}
