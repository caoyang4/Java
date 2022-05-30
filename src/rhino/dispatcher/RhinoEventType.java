package src.rhino.dispatcher;

import src.rhino.RhinoType;

/**
 * @author zhanjun on 2017/08/17.
 */
public interface RhinoEventType {

    /**
     * event parent
     * @return
     */
    RhinoType getRhinoType();

    /**
     * event type
     * @return
     */
    String getType();

    /**
     * event index
     * @return
     */
    int getIndex();

    /**
     * event value
     * @return
     */
    String getValue();

    /**
     * event is notify or not
     * @return
     */
    boolean isNotify();
}
