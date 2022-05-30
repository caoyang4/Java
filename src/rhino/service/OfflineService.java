package src.rhino.service;

import src.rhino.log.Logger;
import src.rhino.log.LoggerFactory;
import src.rhino.util.AppUtils;

/**
 * Created by zhanjun on 2017/7/25.
 */
public class OfflineService extends RhinoService {

    private static final Logger logger = LoggerFactory.getLogger(OfflineService.class);
    private static String method = "offline";

    public OfflineService() {
        super(method);
    }

    public void offline() {
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("&appKey=" + AppUtils.getAppName());
            builder.append("&ip=" + AppUtils.getLocalIp());
            doPost(builder.toString());
        } catch (Exception e) {
            //ignore exception
        }
    }
}
