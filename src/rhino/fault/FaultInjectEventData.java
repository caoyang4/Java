package src.rhino.fault;

import src.rhino.dispatcher.RhinoEventData;

/**
 * @author zhanjun
 * @date 2017/12/01
 */
public class FaultInjectEventData implements RhinoEventData {

    private long id;

    public FaultInjectEventData(long id) {
        this.id = id;
    }

    @Override
    public String toJson() {
        return Long.toString(id);
    }
}
