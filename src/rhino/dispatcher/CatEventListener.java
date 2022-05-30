package src.rhino.dispatcher;

import com.mysql.cj.util.StringUtils;

import src.rhino.log.Logger;
import src.rhino.log.LoggerFactory;

/**
 * @author zhanjun on 2017/08/17.
 */
public class CatEventListener implements RhinoEventListener {

    private static final Logger logger = LoggerFactory.getLogger(CatEventListener.class);
    public static final RhinoEventListener INSTANCE = new CatEventListener();

    @Override
    public void onEvent(String rhinoKey, RhinoEvent event) {
        try {
            RhinoEventType eventType = event.getEventType();
            String preName = event.getPreName();
            StringBuilder eventName = new StringBuilder(rhinoKey);
            eventName.append(".");
            if (StringUtils.isNullOrEmpty(preName)) {
                eventName.append(preName);
                eventName.append(".");
            }
            eventName.append(eventType.getValue());

        } catch (Exception e) {
            logger.warn("CatEventListener onEvent + ", e);
        }
    }
}
