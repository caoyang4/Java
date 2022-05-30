package src.rhino.dispatcher;

/**
 * @author zhanjun on 2017/08/17.
 */
public interface RhinoEventListener {

    /**
     * 处理event
     * @param rhinoKey
     * @param event
     */
    void onEvent(String rhinoKey, RhinoEvent event);
}
