package src.rhino.dispatcher;


import src.rhino.RhinoType;
import src.rhino.log.Logger;
import src.rhino.log.LoggerFactory;
import src.rhino.service.RhinoManager;

/**
 * @author zhanjun on 2017/08/17.
 */
public class NotifyEventListener implements RhinoEventListener {

    private static final Logger logger = LoggerFactory.getLogger(NotifyEventListener.class);
    private static final String NOTIFY_MONITOR_ENABLE_KEY = "rhino.monitor.notify.enable";
    public static final RhinoEventListener INSTANCE = new NotifyEventListener();

    @Override
    public void onEvent(String rhinoKey, RhinoEvent event) {
        try {
            if (event.getEventType().isNotify() && isNotifyEnable()) {
                RhinoEventType eventType = event.getEventType();
                RhinoEventData eventData = event.getEventData();
                RhinoType rhinoType = eventType.getRhinoType();
                String data = "";
                if (eventData != null) {
                    data = eventData.toJson();
                }
                NotifyEvent notifyEvent = new NotifyEvent(rhinoType, rhinoKey, eventType, data);
                RhinoManager.notify(notifyEvent);
            }
        } catch (Exception e) {
            logger.warn("NotifyEventListener onEvent", e);
        }
    }

    private boolean isNotifyEnable() {
        return true;
    }
}
